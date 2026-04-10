# Tasks: Account Dashboard

## T1 [P] baas-onboarding — search endpoint
What: `GET /onboarding/search?email=&phone=&document=` — DynamoDB scan returning first match
Where: OnboardingRepository interface + DynamoOnboardingRepository + new usecase + OnboardingController
Done when: Returns 200 + onboarding JSON on match, 404 on no match, 400 if no query param given

## T2 [P] baas-account — by-onboarding endpoint
What: `GET /accounts/by-onboarding/{onboardingId}` — DynamoDB scan on Account table by onboarding_id
Where: AccountRepository interface + DynamoAccountRepository + AccountController
Done when: Returns 200 + account JSON on match, 404 on no match

## T3 [P] baas-account — CORS fix
What: Add `Idempotency-Key` to CorsConfig allowedHeaders
Where: baas-account/config/CorsConfig.java
Done when: CORS preflight for POST /accounts/{id}/checkin accepts Idempotency-Key header

## T4 [P] nginx — accounts proxy
What: Add `location /accounts { proxy_pass http://host.docker.internal:9003; }` block
Where: baas-frontend/nginx.conf
Done when: nginx routes /accounts/* to port 9003

## T5 api.ts — new API functions
What: Add functions for search-onboarding, get-account-by-onboarding, get-transactions, checkin, checkout
Where: baas-frontend/src/lib/api.ts
Depends on: T1, T2 (interface contract)
Done when: All functions exported with correct types

## T6 AccountDashboard.tsx — main component
What: New page component showing personal data, account info, transactions, transfer form
Where: baas-frontend/src/components/AccountDashboard.tsx
Depends on: T5
Done when: Component renders all sections, transfer works end-to-end

## T7 OnboardingStatus.tsx — add dashboard CTA
What: When status=COMPLETED, show "Open Dashboard" button that calls onDashboard() callback
Where: baas-frontend/src/components/OnboardingStatus.tsx
Done when: Button visible on COMPLETED, hidden otherwise

## T8 App.tsx — wire dashboard view
What: Add 'dashboard' view type, handle transition form→status→dashboard, pass accountId
Where: baas-frontend/src/App.tsx
Depends on: T6, T7
Done when: Full flow works: form → status polling → dashboard on COMPLETED
