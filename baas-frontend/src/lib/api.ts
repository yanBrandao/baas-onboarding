const BASE = ''  // relative — proxied by Vite in dev and nginx in production

export interface OnboardingPayload {
  name: string
  email: string
  phone: string
  document: string
  birth_date: string
  mother_name: string
  fingerprint: string
  address: {
    street: string
    city: string
    state: string
    zip: string
  }
}

export interface OnboardingResponse {
  onboarding_id: string
}

export async function createOnboarding(body: OnboardingPayload): Promise<OnboardingResponse> {
  const res = await fetch(`${BASE}/onboarding`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  })
  if (!res.ok) {
    const text = await res.text().catch(() => res.statusText)
    throw new Error(`${res.status}: ${text}`)
  }
  return res.json()
}
