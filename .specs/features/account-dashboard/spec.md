# Feature: Account Dashboard

## Goal
After onboarding completes, the customer lands on a dashboard showing their personal data,
account balance, transaction history, and a form to transfer money (cash-in / cash-out)
to/from any account in the system identified by email, phone, or document number.

## User Flow
1. Onboarding status → COMPLETED → "Open Dashboard" button appears
2. Dashboard loads: personal data + account info + transaction history
3. Transfer section: enter recipient identifier (email / phone / document) + amount
4. Confirm: checkout from source account → checkin to recipient account

## Requirements

| ID   | Requirement |
|------|-------------|
| R001 | `GET /onboarding/search?email=&phone=&document=` — find onboarding by one contact field |
| R002 | `GET /accounts/by-onboarding/{onboardingId}` — find account by onboardingId |
| R003 | `baas-account` CORS must allow `Idempotency-Key` header (needed for checkin/checkout) |
| R004 | nginx proxies `/accounts` → port 9003 |
| R005 | Dashboard shows: name, email, phone, document, birth_date, address |
| R006 | Dashboard shows: account_id, balance (BRL formatted), status, created_at |
| R007 | Dashboard shows: transaction history (type, amount, date) — polls or loads on mount |
| R008 | Transfer form: recipient identifier type (email/phone/document) + value + amount |
| R009 | Transfer executes: checkout from current account + checkin to recipient account |
| R010 | Transfer uses random UUID as Idempotency-Key per attempt |
| R011 | Transfer gives clear feedback: loading state, success, error (recipient not found, insufficient funds) |
| R012 | After transfer completes, transaction history refreshes automatically |

## Out of Scope
- Atomic server-side transfer (frontend orchestrates two calls)
- Authentication / session management
- Pagination of transaction history
- Currency conversion (all transactions in BRL)

## Key Design Decisions
- D: DynamoDB scan (no GSI) — acceptable at demo scale
- D: Frontend orchestrates transfer: POST checkout → POST checkin
- D: `accountId` stored in localStorage alongside `onboardingId`
- D: Recipient lookup: search onboarding by contact → get account by onboardingId
