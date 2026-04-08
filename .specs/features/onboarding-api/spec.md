# Onboarding API Specification

## Problem Statement

The `baas-onboarding` microservice skeleton exists (Spring Boot 4.0.5 + Java 25) but has no implementation — no controller, no domain objects, no DynamoDB integration, no SNS publishing. The service must accept customer onboarding requests, persist them to DynamoDB, and kick off the downstream workflow by publishing an SNS `FRAUD_CHECK` message.

## Goals

- [ ] Accept POST /onboarding and return the generated onboarding ID
- [ ] Persist onboarding data to the `Onboarding` DynamoDB table
- [ ] Publish a FRAUD_CHECK SNS message using the baas-common message contract
- [ ] Expose GET /onboarding/{id} for full record retrieval
- [ ] Expose GET /onboarding/{id}/status for status-only retrieval

## Out of Scope

| Feature | Reason |
|---|---|
| Downstream microservices (fraud check, customer creation, etc.) | Different modules |
| Input validation / error body format | Not specified |
| Authentication / authorization | Out of scope for this case study |
| Retry / DLQ for SNS failures | Infrastructure concern |

---

## User Stories

### P1: Create Onboarding ⭐ MVP

**User Story**: As a client, I want to POST customer data to /onboarding so that the onboarding workflow is started and I receive a unique onboarding ID.

**Acceptance Criteria**:

1. WHEN POST /onboarding with valid body THEN system SHALL create an Onboarding with a UUID, status IN_PROGRESS
2. WHEN creating onboarding THEN system SHALL publish SNS FRAUD_CHECK message BEFORE saving to DynamoDB
3. WHEN SNS publish succeeds THEN system SHALL persist the Onboarding to DynamoDB table `Onboarding`
4. WHEN onboarding is created THEN system SHALL return HTTP 201 with `{ "onboarding_id": "<uuid>" }`

**SNS message contract**:
```json
{
  "onboarding_id": "...",
  "step": "FRAUD_CHECK",
  "next_steps": ["CUSTOMER_CREATION", "ACCOUNT_CREATION", "ACCOUNT_NOTIFICATION"],
  "data": { "name", "email", "phone", "document", "birth_date", "mother_name", "fingerprint", "address" },
  "metadata": { all steps PENDING, status IN_PROGRESS, created_at/updated_at = now }
}
```

### P1: Get Onboarding Details ⭐ MVP

**User Story**: As a client, I want to GET /onboarding/{id} to retrieve the full onboarding record.

**Acceptance Criteria**:

1. WHEN GET /onboarding/{id} with existing ID THEN system SHALL return HTTP 200 with full onboarding JSON
2. WHEN GET /onboarding/{id} with unknown ID THEN system SHALL return HTTP 404

### P1: Get Onboarding Status ⭐ MVP

**User Story**: As a client, I want to GET /onboarding/{id}/status to check only the current status.

**Acceptance Criteria**:

1. WHEN GET /onboarding/{id}/status with existing ID THEN system SHALL return HTTP 200 with `{ "onboarding_id": "...", "status": "..." }`
2. WHEN GET /onboarding/{id}/status with unknown ID THEN system SHALL return HTTP 404

---

## Edge Cases

- WHEN onboarding_id not provided THEN system SHALL auto-generate a UUID
- WHEN status not set THEN system SHALL default to `IN_PROGRESS`
- WHEN SNS publish fails THEN system SHALL NOT save to DynamoDB (SNS is called first)

---

## Requirement Traceability

| Requirement ID | Story | Status |
|---|---|---|
| ONB-01 | P1: Create Onboarding | Pending |
| ONB-02 | P1: Create Onboarding — SNS before DynamoDB | Pending |
| ONB-03 | P1: Create Onboarding — persist to DynamoDB | Pending |
| ONB-04 | P1: Get Onboarding Details | Pending |
| ONB-05 | P1: Get Onboarding Status | Pending |
