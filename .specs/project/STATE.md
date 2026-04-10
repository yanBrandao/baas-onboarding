# State

## Decisions
- D001: Face photo sent as base64 string in the `fingerprint` field of `POST /onboarding` — avoids multipart complexity, matches existing String field
- D002: Frontend lives at `baas-frontend/` (repo root), Vite dev server on port 5173
- D003: CORS configured globally in baas-onboarding via `WebMvcConfigurer`, allowing `http://localhost:5173`
- D004: Webcam capture uses browser `getUserMedia` API via a custom React hook — no external lib needed
- D005: Photo is shown as a preview before submit; user can retake

## Active Features
- `onboarding-frontend` — COMPLETED
- `baas-account` — COMPLETED
- `kafka-topic-per-service` — COMPLETED
- `extract-kafka-publisher` — COMPLETED

## Decisions (kafka-topic-per-service)
- D009: Each service owns a dedicated topic named after itself (`baas-frauds`, `baas-account`, `baas-webhook`)
- D010: Producer topic (`baas.kafka.topic`) points to the NEXT service's topic; consumer topic is `baas.kafka.consumer-topic` (separate property)
- D011: `@KafkaListener` uses `${baas.kafka.consumer-topic:...}` — decoupled from the publish topic property
- D012: Step-based filtering inside listeners is kept as a defensive guard (not removed)

## Decisions (extract-kafka-publisher)
- D013: `baas-common` is now a pure message-contract library — only `OnboardingEventPublisher` interface + message types; no Kafka infrastructure
- D014: Each publishing service (baas-onboarding, baas-frauds, baas-account) owns a local `KafkaEventPublisher` + `KafkaPublisherConfig`; `spring-kafka` and `spring-boot-autoconfigure` removed from baas-common
- D015: `OnboardingNewCustomerPublisher` (empty stub) was deleted; replaced by `OnboardingKafkaService` in `infrastructure/service/`, wired into `OnboardingNewCustomer` use case
- D016: `OnboardingMessage.onboardingId` serializes as `onboarding_id` (via `@JsonProperty`) — always use snake_case key in JSON assertions

## Decisions (baas-account)
- D006: @DynamoDbVersionAttribute is in package `extensions.annotations`, NOT `mapper.annotations` (AWS SDK 2.25.15)
- D007: transaction_id IS the idempotency key — DynamoDB PK+SK uniqueness enforces deduplication atomically
- D008: OCC retry max 3 attempts; ConditionalCheckFailedException on transaction save = idempotency race (return existing); on balance update = retry loop

## Preferences
- (empty)
