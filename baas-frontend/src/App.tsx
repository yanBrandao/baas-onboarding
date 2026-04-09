import { useState } from 'react'
import { OnboardingForm } from '@/components/OnboardingForm'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import './index.css'

type View = 'form' | 'success'

export default function App() {
  const [view, setView] = useState<View>('form')
  const [onboardingId, setOnboardingId] = useState<string>('')

  function handleSuccess(id: string) {
    setOnboardingId(id)
    setView('success')
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

        {view === 'success' && (
          <Card className="text-center">
            <CardHeader>
              <CardTitle className="text-green-600">Application Submitted!</CardTitle>
            </CardHeader>
            <CardContent className="flex flex-col items-center gap-3">
              <p className="text-muted-foreground text-sm">
                Your onboarding request was received. Your application ID:
              </p>
              <Badge variant="secondary" className="font-mono text-sm px-4 py-1.5">
                {onboardingId}
              </Badge>
              <p className="text-xs text-muted-foreground mt-2">
                We'll process your application and get in touch soon.
              </p>
              <button
                onClick={() => setView('form')}
                className="text-xs text-primary underline mt-2"
              >
                Submit another application
              </button>
            </CardContent>
          </Card>
        )}

      </div>
    </div>
  )
}
