import { useEffect, useState } from 'react'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { Loader2, CheckCircle2, XCircle } from 'lucide-react'

interface OnboardingStatusProps {
  onboardingId: string
  onClear: () => void
}

type Status = 'IN_PROGRESS' | 'COMPLETED' | 'FAILED' | 'UNKNOWN'

interface StatusResponse {
  onboarding_id: string
  status: Status
}

export function OnboardingStatus({ onboardingId, onClear }: OnboardingStatusProps) {
  const [status, setStatus] = useState<Status>('UNKNOWN')
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    let timeoutId: ReturnType<typeof setTimeout>

    const fetchStatus = async () => {
      try {
        const res = await fetch(`/onboarding/${onboardingId}/status`)
        if (res.ok) {
          const data = await res.json() as StatusResponse
          setStatus(data.status)
          
          if (data.status === 'IN_PROGRESS') {
            // Poll again in 3 seconds
            timeoutId = setTimeout(fetchStatus, 3000)
          } else {
            setLoading(false)
          }
        } else {
          console.error("Failed to fetch status", res.status)
          setStatus('UNKNOWN')
          setLoading(false)
        }
      } catch (error) {
        console.error("Error fetching status:", error)
        setStatus('UNKNOWN')
        setLoading(false)
      }
    }

    fetchStatus()

    return () => {
      if (timeoutId) clearTimeout(timeoutId)
    }
  }, [onboardingId])

  return (
    <Card className="text-center mx-auto max-w-sm mt-8 border-2">
      <CardHeader className="pb-4">
        <CardTitle>Application Status</CardTitle>
        <CardDescription>ID: {onboardingId}</CardDescription>
      </CardHeader>
      
      <CardContent className="flex flex-col items-center gap-6">
        {status === 'IN_PROGRESS' && (
          <div className="flex flex-col items-center gap-3 text-blue-600 animate-in fade-in zoom-in duration-500">
            <Loader2 className="h-12 w-12 animate-spin" />
            <p className="font-medium text-lg">Processing...</p>
            <p className="text-sm text-muted-foreground">
              We are verifying your submitted information. This usually takes a few seconds.
            </p>
          </div>
        )}

        {status === 'COMPLETED' && (
          <div className="flex flex-col items-center gap-3 text-green-600 animate-in fade-in zoom-in duration-500">
            <CheckCircle2 className="h-12 w-12" />
            <p className="font-medium text-lg">Approved!</p>
            <p className="text-sm text-foreground">
              Your onboarding has been completed successfully. Welcome aboard!
            </p>
          </div>
        )}

        {status === 'FAILED' && (
          <div className="flex flex-col items-center gap-3 text-red-600 animate-in fade-in zoom-in duration-500">
            <XCircle className="h-12 w-12" />
            <p className="font-medium text-lg">Application Failed</p>
            <p className="text-sm text-foreground">
              Unfortunately, we could not verify your information. Please check your details and try again.
            </p>
          </div>
        )}

        {status === 'UNKNOWN' && !loading && (
          <div className="flex flex-col items-center gap-3 animate-in fade-in zoom-in duration-500">
            <Badge variant="secondary">Unknown Status</Badge>
            <p className="text-sm text-muted-foreground">
              We could not retrieve the status of this application. It may be invalid or expired.
            </p>
          </div>
        )}

        <button
          onClick={onClear}
          className="text-xs text-primary underline mt-4 opacity-80 hover:opacity-100 transition-opacity"
        >
          {status === 'FAILED' || status === 'UNKNOWN' ? 'Start a new application' : 'Clear tracked ID'}
        </button>
      </CardContent>
    </Card>
  )
}
