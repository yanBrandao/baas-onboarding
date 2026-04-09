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

## Decisions (baas-account)
- D006: @DynamoDbVersionAttribute is in package `extensions.annotations`, NOT `mapper.annotations` (AWS SDK 2.25.15)
- D007: transaction_id IS the idempotency key — DynamoDB PK+SK uniqueness enforces deduplication atomically
- D008: OCC retry max 3 attempts; ConditionalCheckFailedException on transaction save = idempotency race (return existing); on balance update = retry loop

## Preferences
- (empty)
