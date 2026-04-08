# Onboarding API Tasks

**Spec**: `.specs/features/onboarding-api/spec.md`
**Status**: In Progress

---

## Execution Plan

```
Phase 1 (Sequential):
  T1 → T2

Phase 2 (Sequential, foundation):
  T2 → T3 → T4 → T5

Phase 3 (Sequential, infrastructure):
  T4 → T6
  T3 → T7
  T5+T6+T7 → T8
  T4 → T9

Phase 4 (Parallel, use cases):
  T8+T9 → T10 [P]
  T8     → T11 [P]

Phase 5 (Sequential, controller):
  T10+T11 → T12
```

---

## Task Breakdown

### T1: Update build.gradle — add AWS SDK and baas-common dependencies

**What**: Add mavenLocal repo, baas-common dependency, AWS SDK BOM + dynamodb-enhanced, and jvmArgs for Java 25
**Where**: `baas-onboarding/build.gradle`
**Depends on**: None
**Requirement**: ONB-01, ONB-02, ONB-03

**Done when**:
- [ ] `mavenLocal()` added to repositories
- [ ] `dependencyManagement` block added with `software.amazon.awssdk:bom:2.25.15`
- [ ] `implementation 'com.tapajos.baas:baas-common:0.0.1-SNAPSHOT'` added
- [ ] `implementation 'software.amazon.awssdk:dynamodb-enhanced'` added
- [ ] `jvmArgs("-Dnet.bytebuddy.experimental=true")` added to test task
- [ ] Gate check passes: `./gradlew build -x test`

**Tests**: none
**Gate**: build

---

### T2: Update application.yaml — add server port and AWS config

**What**: Configure server port 9001, DynamoDB endpoint, SNS topic ARN, and Jackson snake_case
**Where**: `baas-onboarding/src/main/resources/application.yaml`
**Depends on**: None
**Requirement**: ONB-01

**Done when**:
- [ ] `server.port: 9001` set
- [ ] `amazon.dynamodb.endpoint`, `amazon.aws.region`, `amazon.sns.topic.arn` configured
- [ ] `baas.sns.*` properties configured (used by BaasCommonAutoConfiguration)
- [ ] `spring.jackson.property-naming-strategy: SNAKE_CASE` added

**Tests**: none
**Gate**: build

---

### T3: Create Address domain record

**What**: Pure Java record for address data in the domain layer
**Where**: `baas-onboarding/src/main/java/com/tapajos/baas_onboarding/domain/Address.java`
**Depends on**: None
**Requirement**: ONB-01

**Done when**:
- [ ] Record with fields: street, city, state, zip
- [ ] Gate check passes: `./gradlew build -x test`

**Tests**: none
**Gate**: build

---

### T4: Create Onboarding domain record

**What**: Pure Java record that auto-generates UUID if onboardingId is null and defaults status to IN_PROGRESS
**Where**: `baas-onboarding/src/main/java/com/tapajos/baas_onboarding/domain/Onboarding.java`
**Depends on**: T3
**Requirement**: ONB-01

**Done when**:
- [ ] Record with all customer fields + Address + onboardingId + status
- [ ] Compact constructor: if onboardingId==null → UUID.randomUUID().toString(); if status==null → "IN_PROGRESS"
- [ ] Gate check passes: `./gradlew build -x test`

**Tests**: none
**Gate**: build

---

### T5: Create OnboardingRepository interface

**What**: Port (interface) for onboarding persistence
**Where**: `baas-onboarding/src/main/java/com/tapajos/baas_onboarding/repository/OnboardingRepository.java`
**Depends on**: T4
**Requirement**: ONB-03, ONB-04

**Done when**:
- [ ] Interface with `void save(Onboarding onboarding)` and `Optional<Onboarding> findById(String id)`
- [ ] Gate check passes: `./gradlew build -x test`

**Tests**: none
**Gate**: build

---

### T6: Create DynamoConfig — AWS client beans

**What**: Spring @Configuration that wires DynamoDbClient and DynamoDbEnhancedClient
**Where**: `baas-onboarding/src/main/java/com/tapajos/baas_onboarding/infrastructure/config/DynamoConfig.java`
**Depends on**: T1
**Requirement**: ONB-03

**Done when**:
- [ ] `DynamoDbClient` bean using `${amazon.dynamodb.endpoint}` and `${amazon.aws.region:us-east-1}`
- [ ] `DynamoDbEnhancedClient` bean wrapping the client
- [ ] Dummy credentials (accessKey/secretKey) matching LocalStack expectation
- [ ] Gate check passes: `./gradlew build -x test`

**Tests**: none
**Gate**: build

---

### T7: Create OnboardingEntity and AddressEntity — DynamoDB annotated beans

**What**: @DynamoDbBean classes for the Enhanced Client table schema mapping
**Where**:
  - `baas-onboarding/src/main/java/com/tapajos/baas_onboarding/infrastructure/repository/OnboardingEntity.java`
  - `baas-onboarding/src/main/java/com/tapajos/baas_onboarding/infrastructure/repository/AddressEntity.java`
**Depends on**: T3
**Requirement**: ONB-03

**Done when**:
- [ ] `OnboardingEntity`: @DynamoDbBean, `onboardingId` as @DynamoDbPartitionKey with @DynamoDbAttribute("onboarding_id"), all customer fields, `AddressEntity address`
- [ ] `AddressEntity`: @DynamoDbBean, all address fields
- [ ] Public no-arg constructors + getters + setters (JavaBean style)
- [ ] Gate check passes: `./gradlew build -x test`

**Tests**: none
**Gate**: build

---

### T8: Implement DynamoOnboardingRepository

**What**: DynamoDB Enhanced Client adapter implementing OnboardingRepository
**Where**: `baas-onboarding/src/main/java/com/tapajos/baas_onboarding/infrastructure/repository/DynamoOnboardingRepository.java`
**Depends on**: T5, T6, T7
**Requirement**: ONB-03, ONB-04

**Done when**:
- [ ] `@Repository` implementing `OnboardingRepository`
- [ ] Table name `Onboarding`, schema from `TableSchema.fromBean(OnboardingEntity.class)`
- [ ] `save()` calls `table.putItem()`
- [ ] `findById()` calls `table.getItem(Key)` and maps to domain; empty → Optional.empty()
- [ ] Bidirectional mapping: Onboarding ↔ OnboardingEntity
- [ ] Gate check passes: `./gradlew build -x test`

**Tests**: none
**Gate**: build

---

### T9: Implement OnboardingSnsService

**What**: Service that maps a domain Onboarding to an OnboardingMessage and publishes it via OnboardingSnsPublisher
**Where**: `baas-onboarding/src/main/java/com/tapajos/baas_onboarding/infrastructure/service/OnboardingSnsService.java`
**Depends on**: T4, T1 (baas-common on classpath)
**Requirement**: ONB-02

**Done when**:
- [ ] `@Service` wrapping `OnboardingSnsPublisher`
- [ ] `send(Onboarding)` maps domain → baas-common `OnboardingData` (including nested address)
- [ ] Uses `OnboardingMetadata.initial()` for metadata
- [ ] Publishes `OnboardingMessage.of(id, FRAUD_CHECK, data, metadata)` — nextSteps auto-filled
- [ ] Gate check passes: `./gradlew build -x test`

**Tests**: none
**Gate**: build

---

### T10: Implement OnboardingNewCustomer use case + unit test [P]

**What**: Use case that orchestrates: create Onboarding → SNS → DynamoDB; returns onboardingId
**Where**:
  - `baas-onboarding/src/main/java/com/tapajos/baas_onboarding/usecase/OnboardingNewCustomer.java`
  - `baas-onboarding/src/test/java/com/tapajos/baas_onboarding/OnboardingNewCustomerUseCaseTest.java`
**Depends on**: T5, T8 (impl registered), T9
**Requirement**: ONB-01, ONB-02, ONB-03

**Done when**:
- [ ] `@Component` with `OnboardingRepository` and `OnboardingSnsService` injected
- [ ] `execute(OnboardingRequest)` creates domain Onboarding, calls snsService.send() FIRST, then repository.save(), returns onboardingId
- [ ] Unit test: mocks repository + snsService, verifies SNS called before save via InOrder
- [ ] Unit test: verifies returned ID is non-null UUID string
- [ ] Gate check passes: `./gradlew test --tests "*OnboardingNewCustomerUseCaseTest"`
- [ ] Test count: 2 tests pass

**Tests**: unit
**Gate**: quick

---

### T11: Implement GetOnboardingDetails use case [P]

**What**: Use case that retrieves onboarding by ID from the repository
**Where**: `baas-onboarding/src/main/java/com/tapajos/baas_onboarding/usecase/GetOnboardingDetails.java`
**Depends on**: T5
**Requirement**: ONB-04, ONB-05

**Done when**:
- [ ] `@Component` with `OnboardingRepository` injected
- [ ] `execute(String id)` delegates to `repository.findById(id)`, returns `Optional<Onboarding>`
- [ ] Gate check passes: `./gradlew build -x test`

**Tests**: none
**Gate**: build

---

### T12: Create OnboardingController + request/response DTOs + @WebMvcTest

**What**: REST controller with 3 endpoints + request/response DTOs + controller test
**Where**:
  - `baas-onboarding/src/main/java/com/tapajos/baas_onboarding/controller/OnboardingController.java`
  - `baas-onboarding/src/main/java/com/tapajos/baas_onboarding/controller/OnboardingRequest.java`
  - `baas-onboarding/src/main/java/com/tapajos/baas_onboarding/controller/AddressRequest.java`
  - `baas-onboarding/src/main/java/com/tapajos/baas_onboarding/controller/OnboardingStatusResponse.java`
  - `baas-onboarding/src/test/java/com/tapajos/baas_onboarding/OnboardingControllerTest.java`
**Depends on**: T10, T11
**Requirement**: ONB-01, ONB-04, ONB-05

**Done when**:
- [ ] `POST /onboarding` → calls `OnboardingNewCustomer.execute()`, returns 201 `{ "onboarding_id": "..." }`
- [ ] `GET /onboarding/{id}` → calls `GetOnboardingDetails.execute()`, returns 200 or 404
- [ ] `GET /onboarding/{id}/status` → returns 200 `{ "onboarding_id": "...", "status": "..." }` or 404
- [ ] `OnboardingRequest` + `AddressRequest` records match the JSON contract (snake_case)
- [ ] `OnboardingStatusResponse` record with `@JsonProperty("onboarding_id")`
- [ ] `@WebMvcTest(OnboardingController.class)` + `@MockitoBean` for use cases
- [ ] Tests: shouldCreateOnboardingAndReturnId, shouldGetOnboardingById, shouldGetOnboardingStatus, shouldReturn404WhenNotFound (for GET endpoints)
- [ ] Gate check passes: `./gradlew test`
- [ ] Test count: ≥6 tests pass

**Tests**: unit (@WebMvcTest)
**Gate**: quick

---

## Parallel Execution Map

```
Phase 1:
  T1 ──→ T2 (both can start immediately, T1 needed for build)

Phase 2:
  T1 ──→ T3 ──→ T4 ──→ T5
                    └──→ T7

Phase 3:
  T1+T4 ──→ T6
  T5+T6+T7 ──→ T8
  T4+T1 ──→ T9

Phase 4 (Parallel):
  T8+T9 ──→ T10 [P]
  T8 ──────→ T11 [P]

Phase 5:
  T10+T11 ──→ T12
```
