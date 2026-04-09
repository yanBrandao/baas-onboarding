import { useEffect, useState, useRef } from 'react'
import { useWebcam } from '@/hooks/useWebcam'
import { Button } from '@/components/ui/button'

type State = 'idle' | 'streaming' | 'captured'

interface Props {
  onCapture: (base64: string | null) => void
}

export function WebcamCapture({ onCapture }: Props) {
  const [state, setState] = useState<State>('idle')
  const [preview, setPreview] = useState<string | null>(null)
  
  const videoRef = useRef<HTMLVideoElement>(null)
  const { stream, start, capture, release } = useWebcam()

  // Clean up on unmount
  useEffect(() => {
    return () => { release() }
  }, [release])

  // Handle playing stream when available
  useEffect(() => {
    if (videoRef.current && stream) {
      videoRef.current.srcObject = stream
      // Ensure we explicitly play the video
      videoRef.current.play().catch(e => console.error("Video play failed", e))
    }
  }, [stream])

  // Start the stream once when transitioning to 'streaming'
  useEffect(() => {
    if (state === 'streaming' && !stream) {
      start()
    }
  }, [state, start, stream])

  function handleCapture() {
    const base64 = capture(videoRef.current)
    if (!base64) return
    setPreview(base64)
    setState('captured')
    release()
    onCapture(base64)
  }

  function handleRetake() {
    setPreview(null)
    setState('idle')
    onCapture(null)
  }

  return (
    <div className="flex flex-col items-center gap-3">
      {/* 
        Always in the DOM to avoid ref missing issues. 
        Hidden when not in streaming state.
      */}
      <video
        ref={videoRef}
        autoPlay
        playsInline
        muted
        className={`w-64 h-48 rounded-lg object-cover border${state !== 'streaming' ? ' hidden' : ''}`}
      />

      {state === 'idle' && (
        <>
          <div className="w-64 h-48 rounded-lg border-2 border-dashed border-muted-foreground/30 flex items-center justify-center bg-muted/30">
            <span className="text-sm text-muted-foreground">Camera preview</span>
          </div>
          <Button type="button" variant="outline" onClick={() => setState('streaming')}>
            Enable Camera
          </Button>
        </>
      )}

      {state === 'streaming' && (
        <Button type="button" onClick={handleCapture}>
          Capture Photo
        </Button>
      )}

      {state === 'captured' && preview && (
        <>
          <img
            src={preview}
            alt="Captured photo"
            className="w-64 h-48 rounded-lg object-cover border"
          />
          <Button type="button" variant="outline" onClick={handleRetake}>
            Retake
          </Button>
        </>
      )}
    </div>
  )
}
