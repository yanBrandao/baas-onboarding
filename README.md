# 🏦 baas-onboarding

A case study of a **bank account onboarding** system, inspired by real-world BaaS (Banking as a Service) architecture. The project simulates an event-driven workflow that takes a customer from initial request all the way to a fully provisioned bank account.

---

## 🏗️ Architecture

![Architecture](./docs/architecture.drawio.png)

The onboarding flow is orchestrated through **SNS → SQS** message passing across four independent microservices. `baas-onboarding` is the entry point: it receives the customer data, generates an `onboarding_id`, persists it to **DynamoDB**, and publishes the first event to kick off the workflow.

Each downstream microservice consumes from its own SQS queue, processes its step, and publishes the next message back to SNS — carrying the current state forward.

### 🔄 Workflow steps

```
[HTTP Request]
      │
      ▼
baas-onboarding ──► SNS ──► FRAUD_CHECK
                                │
                                ▼
                         CUSTOMER_CREATION
                                │
                                ▼
                         ACCOUNT_CREATION
                                │
                                ▼
                        ACCOUNT_NOTIFICATION
```

---

## 📦 Modules

| Module | Description |
|---|---|
| `baas-onboarding` | REST entry point — receives requests, stores state in DynamoDB, fires the first SNS event |
| `baas-common` | Shared library — canonical message types, step enums, and `OnboardingSnsPublisher` used by all microservices |

---

## 📨 Message Contract

Every message published to SNS follows this structure:

```json
{
    "onboarding_id": "1234567890",
    "step": "FRAUD_CHECK",
    "next_steps": [
        "CUSTOMER_CREATION",
        "ACCOUNT_CREATION",
        "ACCOUNT_NOTIFICATION"
    ],
    "data": {
        "name": "John Doe",
        "email": "johndoe@domain.com",
        "phone": "1234567890",
        "document": "1234567890",
        "birth_date": "1990-01-01",
        "mother_name": "Jane Doe",
        "fingerprint": "base64_encoded_fingerprint",
        "address": {
            "street": "123 Main St",
            "city": "New York",
            "state": "NY",
            "zip": "12345"
        }
    },
    "metadata": {
        "fraud_check": {
            "status": "PENDING",
            "message": ""
        },
        "customer_creation": {
            "status": "PENDING",
            "message": ""
        },
        "account_creation": {
            "status": "PENDING",
            "message": ""
        },
        "error": {
            "code": "",
            "message": ""
        },
        "status": "IN_PROGRESS",
        "created_at": "2022-01-01T00:00:00Z",
        "updated_at": "2022-01-01T00:00:00Z"
    }
}
```

- **`step`** — the step currently being processed
- **`next_steps`** — remaining steps in the pipeline; each microservice pops the first one to know where to route next
- **`metadata`** — tracks the result of each step and the overall onboarding status (`IN_PROGRESS`, `COMPLETED`, `FAILED`)

---

## ⚙️ Tech Stack

- ☕ **Java 25** + **Spring Boot**
- 🗄️ **DynamoDB** — onboarding state persistence
- 📣 **SNS / SQS** — event-driven communication between microservices
- 🐳 **LocalStack** — local AWS emulation via Docker

---

## 🚀 Running Locally

**1. Start LocalStack**

```bash
docker compose up -d
```

This automatically creates the `Onboarding` DynamoDB table and the `baas-onboarding` SNS topic via the init script at `localstack/init-scripts/init-aws.sh`.

**2. Run the service**

```bash
cd baas-onboarding
./gradlew bootRun
```

The API will be available at `http://localhost:9001`.

---

## 🧪 Running Tests

```bash
cd baas-onboarding
./gradlew test
```

---

## 🌐 API

### `POST /onboarding`
Creates a new onboarding process.

**Response** `201 Created`
```json
{ "onboarding_id": "..." }
```

### `GET /onboarding/{onboardingId}`
Returns the full onboarding record.

### `GET /onboarding/{onboardingId}/status`
Returns only the current status.

```json
{
    "onboarding_id": "...",
    "status": "IN_PROGRESS"
}
```
