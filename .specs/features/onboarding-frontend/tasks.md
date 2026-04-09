# Tasks: Onboarding Frontend

## T-01 — Add CORS config to baas-onboarding
- **What:** Create `CorsConfig.java` WebMvcConfigurer allowing `http://localhost:5173`
- **Where:** `baas-onboarding/src/main/java/com/tapajos/baas_onboarding/config/CorsConfig.java`
- **Covers:** B-01, B-02
- **Done when:** `OPTIONS /onboarding` from localhost:5173 returns 200 with correct headers
- **Status:** completed

## T-02 — Scaffold baas-frontend project
- **What:** `npm create vite@latest baas-frontend -- --template react-ts`, add Tailwind, init shadcn
- **Where:** `baas-frontend/` (repo root)
- **Done when:** `npm run dev` starts without errors; blank page renders at localhost:5173
- **Status:** completed

## T-03 — Add shadcn components + install deps
- **What:** `npx shadcn@latest add button input label card badge separator`; install `react-hook-form`, `zod`, `@hookform/resolvers`
- **Depends on:** T-02
- **Done when:** All imports resolve; `npm run build` succeeds
- **Status:** completed

## T-04 — Implement `useWebcam` hook
- **What:** Custom hook — `start`, `capture` (returns base64), `release`
- **Where:** `baas-frontend/src/hooks/useWebcam.ts`
- **Covers:** W-01..W-04
- **Depends on:** T-02
- **Done when:** Hook exported and TypeScript compiles
- **Status:** completed

## T-05 — Implement `WebcamCapture` component
- **What:** `idle → streaming → captured` state machine; emits `onCapture(base64 | null)`
- **Where:** `baas-frontend/src/components/WebcamCapture.tsx`
- **Covers:** W-01..W-05
- **Depends on:** T-03, T-04
- **Done when:** Component renders; capture emits base64; retake resets
- **Status:** completed

## T-06 — Implement `api.ts`
- **What:** `createOnboarding(payload)` fetch wrapper targeting `http://localhost:9001`
- **Where:** `baas-frontend/src/lib/api.ts`
- **Depends on:** T-02
- **Done when:** Function exported with correct TypeScript types
- **Status:** completed

## T-07 — Implement `OnboardingForm` component
- **What:** Full form with all F-01..F-10 fields + zod schema + react-hook-form; embeds WebcamCapture; calls api.ts on submit
- **Where:** `baas-frontend/src/components/OnboardingForm.tsx`
- **Covers:** F-01..F-10, W-05, S-01..S-04
- **Depends on:** T-03, T-05, T-06
- **Done when:** Form validates, submits, and shows success/error state
- **Status:** completed

## T-08 — Wire App.tsx + global styles
- **What:** `App.tsx` renders `OnboardingForm` (form state) or success card (success state); import Tailwind in `index.css`
- **Where:** `baas-frontend/src/App.tsx`, `src/index.css`
- **Depends on:** T-07
- **Done when:** `npm run build` succeeds; page renders the form
- **Status:** completed
