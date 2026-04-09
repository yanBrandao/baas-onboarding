# Design: Onboarding Frontend

## Architecture

```
baas-frontend/          ← Vite + React + TS + Tailwind + shadcn/ui
  src/
    components/
      OnboardingForm.tsx      ← main form (all personal + address fields)
      WebcamCapture.tsx       ← webcam feed + capture + retake
    hooks/
      useWebcam.ts            ← getUserMedia, captureFrame, release
    lib/
      api.ts                  ← fetch wrapper → POST /onboarding
      utils.ts                ← shadcn cn() helper
    App.tsx                   ← routes: form view / success view
    main.tsx

baas-onboarding/
  src/main/java/.../config/
    CorsConfig.java           ← WebMvcConfigurer allowing localhost:5173
```

## Component Breakdown

### `useWebcam` hook
- `videoRef` — attached to `<video>` element
- `start()` — calls `getUserMedia({ video: true })`, streams to videoRef
- `capture()` → returns base64 JPEG string (`canvas.toDataURL('image/jpeg')`)
- `release()` — stops all tracks on unmount

### `WebcamCapture`
- State: `idle | streaming | captured`
- `idle` → shows "Enable Camera" button
- `streaming` → shows `<video>` + "Capture" button
- `captured` → shows `<img>` preview + "Retake" button
- Emits `onCapture(base64: string | null)` to parent

### `OnboardingForm`
- Uses `react-hook-form` + `zod` for validation
- Personal data section (F-01..F-06) — shadcn `Input` + `Label`
- Address section (F-07..F-10) — same
- Embeds `<WebcamCapture>` — stores captured base64 in form state
- On submit: builds request body, calls `api.createOnboarding()`
- Passes `isLoading` to submit button

### `App`
- State: `form | success`
- `success` renders `onboarding_id` in a shadcn `Card`

### `api.ts`
```ts
const BASE = 'http://localhost:9001'

export async function createOnboarding(body: OnboardingPayload): Promise<{ onboarding_id: string }> {
  const res = await fetch(`${BASE}/onboarding`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  })
  if (!res.ok) throw new Error(`${res.status} ${res.statusText}`)
  return res.json()
}
```

## Backend CORS

`CorsConfig.java` — single `WebMvcConfigurer` bean:
- Path: `/**`
- Allowed origins: `http://localhost:5173`
- Allowed methods: `GET`, `POST`, `PATCH`, `OPTIONS`
- Allowed headers: `Content-Type`, `Accept`
- Max age: 3600s

## shadcn Components Used
- `Button`, `Input`, `Label`, `Card`, `CardHeader`, `CardContent`, `CardFooter`, `Badge`, `Separator`
