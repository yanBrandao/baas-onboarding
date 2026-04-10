import { useState } from 'react'
import { OnboardingForm } from '@/components/OnboardingForm'
import { OnboardingStatus } from '@/components/OnboardingStatus'
import './index.css'

type View = 'form' | 'status'

const STORAGE_KEY = 'baas_onboarding_id'

export default function App() {
  const [onboardingId, setOnboardingId] = useState<string>(() => {
    return localStorage.getItem(STORAGE_KEY) || ''
  })
  
  const [view, setView] = useState<View>(() => {
    return localStorage.getItem(STORAGE_KEY) ? 'status' : 'form'
  })

  function handleSuccess(id: string) {
    setOnboardingId(id)
    localStorage.setItem(STORAGE_KEY, id)
    setView('status')
  }

  function handleClear() {
    localStorage.removeItem(STORAGE_KEY)
    setOnboardingId('')
    setView('form')
  }

  return (
    <div className="min-h-screen bg-background py-10 px-4">
      <div className="max-w-2xl mx-auto flex flex-col gap-6">

        <div className="text-center">
          <h1 className="text-2xl font-semibold tracking-tight">Account Onboarding</h1>
          <p className="text-muted-foreground text-sm mt-1">
            Fill in your details to open a new bank account
          </p>
        </div>

        {view === 'form' && <OnboardingForm onSuccess={handleSuccess} />}

        {view === 'status' && onboardingId && (
          <OnboardingStatus onboardingId={onboardingId} onClear={handleClear} />
        )}

      </div>
    </div>
  )
}
