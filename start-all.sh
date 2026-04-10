#!/usr/bin/env bash
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SKIP_BUILD=false

SERVICES=(
  "baas-onboarding:9001"
  "baas-frauds:9002"
  "baas-account:9003"
  "baas-webhook:9004"
)

LOG_DIR="$REPO_ROOT/.logs"
mkdir -p "$LOG_DIR"

# ── helpers ────────────────────────────────────────────────────────────────────

log()  { echo "[$(date '+%H:%M:%S')] $*"; }
ok()   { echo "[$(date '+%H:%M:%S')] ✓ $*"; }
fail() { echo "[$(date '+%H:%M:%S')] ✗ $*" >&2; }

usage() {
  cat <<EOF
Usage: $(basename "$0") [--skip-build]

Options:
  --skip-build   Skip the build step and use existing jars
  -h, --help     Show this help message
EOF
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --skip-build)
      SKIP_BUILD=true
      shift
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      fail "Unknown option: $1"
      usage
      exit 1
      ;;
  esac
done

wait_for_health() {
  local name="$1" port="$2"
  local url="http://localhost:${port}/actuator/health"
  local retries=30 interval=3

  log "Waiting for $name on $url ..."
  for ((i = 1; i <= retries; i++)); do
    if curl -sf "$url" | grep -q '"status":"UP"'; then
      ok "$name is UP"
      return 0
    fi
    sleep "$interval"
  done

  fail "$name did not become healthy after $((retries * interval))s"
  return 1
}

find_jar() {
  local name="$1"
  local jar
  jar=$(ls "$REPO_ROOT/$name/build/libs/"*.jar 2>/dev/null | grep -v '\-plain\.jar' | head -1)
  if [[ -z "$jar" ]]; then
    fail "No jar found for $name — run without --skip-build"
    exit 1
  fi
  echo "$jar"
}

# ── 1. build all microservices ─────────────────────────────────────────────────

if [[ "$SKIP_BUILD" == true ]]; then
  log "Skipping build phase (--skip-build)"
else
  log "Publishing baas-common to Maven local ..."
  (cd "$REPO_ROOT/baas-common" && ./gradlew publishToMavenLocal -q)
  ok "baas-common published"

  for entry in "${SERVICES[@]}"; do
    name="${entry%%:*}"
    log "Building $name ..."
    (cd "$REPO_ROOT/$name" && ./gradlew clean build -x test -q) || {
      fail "Build failed for $name. Run: cd $name && ./gradlew clean build"
      exit 1
    }
    ok "$name built"
  done
fi

# ── 2. docker compose ──────────────────────────────────────────────────────────

log "Starting docker compose ..."
docker compose -f "$REPO_ROOT/docker-compose.yml" up -d

log "Waiting for LocalStack to be ready ..."
for ((i = 1; i <= 20; i++)); do
  if curl -sf http://localhost:4566/_localstack/health | grep -q '"dynamodb"'; then
    ok "LocalStack is ready"
    break
  fi
  if [[ $i -eq 20 ]]; then
    fail "LocalStack did not become ready in time"
    exit 1
  fi
  sleep 3
done

log "Waiting for Kafka to be ready on localhost:9092 ..."
for ((i = 1; i <= 30; i++)); do
  if bash -c 'echo > /dev/tcp/localhost/9092' 2>/dev/null; then
    ok "Kafka is ready"
    break
  fi
  if [[ $i -eq 30 ]]; then
    fail "Kafka did not become ready in time — check: docker compose logs kafka"
    exit 1
  fi
  sleep 3
done

log "Waiting for Prometheus to be ready on http://localhost:9090 ..."
for ((i = 1; i <= 20; i++)); do
  if curl -sf http://localhost:9090/-/ready > /dev/null; then
    ok "Prometheus is UP  (http://localhost:9090)"
    break
  fi
  if [[ $i -eq 20 ]]; then
    fail "Prometheus did not become ready in time — check: docker compose logs prometheus"
    exit 1
  fi
  sleep 3
done

log "Waiting for Grafana to be ready on http://localhost:3001 ..."
for ((i = 1; i <= 20; i++)); do
  if curl -sf http://localhost:3001/api/health > /dev/null; then
    ok "Grafana is UP  (http://localhost:3001)"
    break
  fi
  if [[ $i -eq 20 ]]; then
    fail "Grafana did not become ready in time — check: docker compose logs grafana"
    exit 1
  fi
  sleep 3
done

log "Waiting for frontend (nginx) to be ready on http://localhost:3000 ..."
for ((i = 1; i <= 20; i++)); do
  if curl -sf http://localhost:3000 > /dev/null; then
    ok "Frontend is UP  (http://localhost:3000)"
    break
  fi
  if [[ $i -eq 20 ]]; then
    fail "Frontend did not become ready in time — check: docker compose logs frontend"
    exit 1
  fi
  sleep 3
done

# ── 3. start microservices ─────────────────────────────────────────────────────

for entry in "${SERVICES[@]}"; do
  name="${entry%%:*}"
  port="${entry##*:}"
  log_file="$LOG_DIR/${name}.log"
  jar=$(find_jar "$name")

  log "Starting $name (port $port) from $jar ..."
  java -jar "$jar" > "$log_file" 2>&1 &
  echo $! > "$LOG_DIR/${name}.pid"

  wait_for_health "$name" "$port" || {
    fail "Startup failed. Check logs at $log_file"
    exit 1
  }
done

# ── 4. final health summary ────────────────────────────────────────────────────

echo ""
log "=== Health check summary ==="
all_ok=true
for entry in "${SERVICES[@]}"; do
  name="${entry%%:*}"
  port="${entry##*:}"
  response=$(curl -sf "http://localhost:${port}/actuator/health" 2>/dev/null || echo '{"status":"UNREACHABLE"}')
  status=$(echo "$response" | grep -o '"status":"[^"]*"' | head -1 | cut -d'"' -f4)
  if [[ "$status" == "UP" ]]; then
    ok "$name → $status  (http://localhost:${port}/actuator/health)"
  else
    fail "$name → $status  (http://localhost:${port}/actuator/health)"
    all_ok=false
  fi
done

frontend_status=$(curl -sf http://localhost:3000 > /dev/null 2>&1 && echo "UP" || echo "UNREACHABLE")
if [[ "$frontend_status" == "UP" ]]; then
  ok "frontend        → UP  (http://localhost:3000)"
else
  fail "frontend        → UNREACHABLE  (http://localhost:3000)"
  all_ok=false
fi

echo ""
if $all_ok; then
  ok "All services are UP"
else
  fail "One or more services are not healthy"
  exit 1
fi

# ── cleanup hint ───────────────────────────────────────────────────────────────

echo ""
log "PIDs stored in $LOG_DIR/*.pid — to stop Spring Boot services:"
log "  kill \$(cat $LOG_DIR/*.pid) && docker compose -f $REPO_ROOT/docker-compose.yml down"
