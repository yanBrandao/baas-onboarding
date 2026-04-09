import { useState, useCallback } from 'react'

export function useWebcam() {
  const [stream, setStream] = useState<MediaStream | null>(null)

  const start = useCallback(async () => {
    try {
      const mediaStream = await navigator.mediaDevices.getUserMedia({ video: true })
      setStream(mediaStream)
    } catch (err) {
      console.error('Failed to access camera:', err)
      alert(`Could not access camera: ${err instanceof Error ? err.message : String(err)}`)
    }
  }, [])

  const capture = useCallback((videoElement: HTMLVideoElement | null): string | null => {
    if (!videoElement) return null

    const canvas = document.createElement('canvas')
    canvas.width = videoElement.videoWidth
    canvas.height = videoElement.videoHeight
    canvas.getContext('2d')?.drawImage(videoElement, 0, 0)
    return canvas.toDataURL('image/jpeg', 0.8)
  }, [])

  const release = useCallback(() => {
    setStream((currentStream) => {
      if (currentStream) {
        currentStream.getTracks().forEach(t => t.stop())
      }
      return null
    })
  }, [])

  return { stream, start, capture, release }
}
