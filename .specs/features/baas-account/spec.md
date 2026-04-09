# Feature: baas-account

## Goal
New microservice handling bank account lifecycle: creation, balance management (deposit/withdraw), block/unblock, and transaction history. Multi-currency support. Guaranteed idempotency and concurrency safety on all balance mutations.

## Port: 9003

## API

| ID | Method | Path | Description |
|---|---|---|---|
| A-01 | POST | `/accounts` | Create account linked to an onboarding_id |
| A-02 | GET | `/accounts/{id}` | Get account + balance |
| A-03 | POST | `/accounts/{id}/checkin` | Deposit money (requires `Idempotency-Key` header) |
| A-04 | POST | `/accounts/{id}/checkout` | Withdraw money (requires `Idempotency-Key` header) |
| A-05 | PATCH | `/accounts/{id}/block` | Block account |
| A-06 | PATCH | `/accounts/{id}/unblock` | Unblock account |
| A-07 | GET | `/accounts/{id}/transactions` | Full transaction history |

## Domain Rules

| ID | Rule |
|---|---|
| D-01 | Account is created with zero balance and status ACTIVE |
| D-02 | Currency is set at creation; transactions must match account currency |
| D-03 | Checkout must be rejected if balance < amount (HTTP 422) |
| D-04 | Checkin/Checkout on a BLOCKED account must be rejected (HTTP 422) |
| D-05 | `Idempotency-Key` header is required for checkin/checkout (HTTP 400 if missing) |
| D-06 | Same `Idempotency-Key` on same account returns the original transaction (idempotent) |
| D-07 | Concurrent balance mutations on the same account are safe via OCC retry (up to 3 attempts) |
| D-08 | After 3 failed OCC retries, return HTTP 409 Conflict |

## Currencies
`BRL`, `USD`, `EUR`, `GBP`

## DynamoDB Tables

### Account (PK: account_id)
`account_id`, `onboarding_id`, `currency`, `balance`, `status`, `version` (OCC), `created_at`

### Transaction (PK: account_id, SK: transaction_id)
`account_id`, `transaction_id` (= client Idempotency-Key), `type` (CHECKIN/CHECKOUT), `amount`, `currency`, `created_at`
