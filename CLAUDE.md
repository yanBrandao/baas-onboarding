# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

A bank account onboarding case study built with Spring Boot 3.4.4 + Java 25. The service accepts onboarding requests, persists them in DynamoDB, and publishes an SNS message to kick off a downstream workflow (fraud check → customer creation → account creation → notification).

Local AWS infrastructure (DynamoDB + SNS) is provided via LocalStack running in Docker.

## Commands

All commands run from `baas-onboarding/` (the Gradle project root):

```bash
# Build
./gradlew build

# Run the application
./gradlew bootRun

# Run all tests
./gradlew test

# Run a single test class
./gradlew test --tests "com.tapajos.baas.onboarding.OnboardingControllerTest"

# Run a single test method
./gradlew test --tests "com.tapajos.baas.onboarding.OnboardingControllerTest.shouldCreateOnboardingAndReturnId"
```

Start LocalStack before running the application locally:

```bash
# From the repo root
docker compose up -d
```

LocalStack init script (`localstack/init-scripts/init-aws.sh`) auto-creates the `Onboarding` DynamoDB table and `baas-onboarding` SNS topic on startup.

## Modules

| Directory | Role |
|---|---|
| `baas-onboarding/` | Entry-point microservice — accepts HTTP requests, persists to DynamoDB, kicks off the workflow via SNS |
| `baas-common/` | Shared library — canonical message types, SNS publisher, Spring Boot autoconfiguration |

## Architecture

The service follows a clean layered architecture:

```
controller → usecase → repository (interface)
                ↓
         infrastructure/
           repository (DynamoDB impl)
           service (SNS)
           config (AWS clients)
```

**Key layers:**

- `domain/` — pure Java records (`Onboarding`, `Address`). `Onboarding` auto-generates a UUID if `onboardingId` is null and defaults `status` to `IN_PROGRESS`.
- `usecase/` — orchestration logic. `OnboardingNewCustomer` sends SNS first, then saves to DynamoDB. `GetOnboardingDetails` delegates to the repository.
- `repository/` — `OnboardingRepository` interface (port).
- `infrastructure/repository/` — `DynamoOnboardingRepository` (DynamoDB Enhanced Client adapter using `OnboardingEntity`/`AddressEntity` annotated beans).
- `infrastructure/service/` — `OnboardingSnsService` builds the full workflow JSON payload (with `step`, `next_steps`, `data`, `metadata`) and publishes to SNS.
- `infrastructure/config/` — `DynamoConfig` wires `DynamoDbClient`, `DynamoDbEnhancedClient`, and `SnsClient` all pointing to `${amazon.dynamodb.endpoint}` (defaults to `http://localhost:4566`).

## Using baas-common in a new microservice

1. Publish to Maven local (run once after any change):
   ```bash
   cd baas-common && ./gradlew publishToMavenLocal
   ```

2. Add to the microservice's `build.gradle`:
   ```groovy
   repositories { mavenLocal(); mavenCentral() }
   dependencies {
       implementation 'com.tapajos.baas:baas-common:0.0.1-SNAPSHOT'
   }
   ```

3. Configure in `application.yaml`:
   ```yaml
   baas:
     sns:
       topic-arn: arn:aws:sns:us-east-1:000000000000:baas-onboarding
       endpoint: http://localhost:4566
       region: us-east-1
   ```

4. Inject and use:
   ```java
   @Autowired OnboardingSnsPublisher publisher;

   OnboardingMessage msg = OnboardingMessage.of(
       message.onboardingId(),
       OnboardingStep.FRAUD_CHECK,       // current step
       message.data(),
       metadata
   );
   publisher.publish(msg);
   ```
   `OnboardingStep.nextSteps()` fills `next_steps` automatically based on the step's position in the workflow.

### baas-common internals

- `message/` — all message records and enums:
  - `OnboardingMessage` — root record matching the README JSON contract
  - `OnboardingStep` — `FRAUD_CHECK → CUSTOMER_CREATION → ACCOUNT_CREATION → ACCOUNT_NOTIFICATION`; `nextSteps()` returns the remainder
  - `OnboardingMetadata` — per-step `StepResult` + overall status + timestamps; `initial()` creates all-PENDING state
  - `StepResult` — `pending()` / `completed(msg)` / `failed(msg)` factory methods
- `sns/OnboardingSnsPublisher` — serializes `OnboardingMessage` to JSON and calls `SnsClient.publish()`
- `autoconfigure/BaasCommonAutoConfiguration` — auto-wires `SnsClient` + `OnboardingSnsPublisher`; both are `@ConditionalOnMissingBean` so services can override them

## AWS / LocalStack Configuration

The app connects to LocalStack using dummy credentials (`accessKey`/`secretKey`). Relevant `application.yaml` properties (not yet present but expected by `DynamoConfig`):

- `amazon.dynamodb.endpoint` — default `http://localhost:4566`
- `amazon.aws.region` — default `us-east-1`
- `amazon.sns.topic.arn` — default `arn:aws:sns:us-east-1:000000000000:baas-onboarding`

## API

- `POST /onboarding` — create onboarding, returns `{ "onboarding_id": "..." }`
- `GET /onboarding/{id}` — full onboarding record
- `GET /onboarding/{id}/status` — only `onboarding_id` + `status`

Server runs on port `9001`.

## Testing Approach

- Controller tests use `@WebMvcTest` + `@MockitoBean` (Spring Boot 3.4+ annotation) — use cases are mocked, no AWS connection needed.
- Use case tests (`OnboardingNewCustomerUseCaseTest`) mock the repository and SNS service directly.
- Tests require `jvmArgs("-Dnet.bytebuddy.experimental=true")` due to Java 25 compatibility; this is already set in `build.gradle`.
