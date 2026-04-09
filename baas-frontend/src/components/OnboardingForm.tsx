import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Separator } from '@/components/ui/separator'
import { WebcamCapture } from '@/components/WebcamCapture'
import { createOnboarding } from '@/lib/api'

const schema = z.object({
  name: z.string().min(1, 'Full name is required'),
  email: z.string().email('Invalid email address'),
  phone: z.string().min(1, 'Phone is required'),
  document: z.string().min(1, 'Document is required'),
  birth_date: z.string().min(1, 'Birth date is required'),
  mother_name: z.string().min(1, "Mother's name is required"),
  street: z.string().min(1, 'Street is required'),
  city: z.string().min(1, 'City is required'),
  state: z.string().min(1, 'State is required'),
  zip: z.string().min(1, 'ZIP is required'),
})

type FormValues = z.infer<typeof schema>

interface Props {
  onSuccess: (onboardingId: string) => void
}

export function OnboardingForm({ onSuccess }: Props) {
  const [photo, setPhoto] = useState<string | null>(null)
  const [serverError, setServerError] = useState<string | null>(null)

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<FormValues>({ resolver: zodResolver(schema) })

  async function onSubmit(values: FormValues) {
    if (!photo) return
    setServerError(null)
    try {
      const result = await createOnboarding({
        name: values.name,
        email: values.email,
        phone: values.phone,
        document: values.document,
        birth_date: values.birth_date,
        mother_name: values.mother_name,
        fingerprint: photo,
        address: {
          street: values.street,
          city: values.city,
          state: values.state,
          zip: values.zip,
        },
      })
      onSuccess(result.onboarding_id)
    } catch (err) {
      setServerError(err instanceof Error ? err.message : 'Unexpected error')
    }
  }

  function field(id: keyof FormValues, label: string, type = 'text') {
    return (
      <div className="flex flex-col gap-1.5">
        <Label htmlFor={id}>{label}</Label>
        <Input id={id} type={type} {...register(id)} />
        {errors[id] && (
          <p className="text-sm text-destructive">{errors[id]?.message}</p>
        )}
      </div>
    )
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} noValidate>
      <div className="flex flex-col gap-6">

        {/* Personal data */}
        <Card>
          <CardHeader>
            <CardTitle className="text-base">Personal Information</CardTitle>
          </CardHeader>
          <CardContent className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {field('name', 'Full Name')}
            {field('email', 'Email', 'email')}
            {field('phone', 'Phone')}
            {field('document', 'Document / CPF')}
            {field('birth_date', 'Birth Date', 'date')}
            {field('mother_name', "Mother's Name")}
          </CardContent>
        </Card>

        {/* Address */}
        <Card>
          <CardHeader>
            <CardTitle className="text-base">Address</CardTitle>
          </CardHeader>
          <CardContent className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="md:col-span-2">{field('street', 'Street')}</div>
            {field('city', 'City')}
            {field('state', 'State')}
            {field('zip', 'ZIP Code')}
          </CardContent>
        </Card>

        {/* Photo */}
        <Card>
          <CardHeader>
            <CardTitle className="text-base">Face Photo</CardTitle>
          </CardHeader>
          <CardContent className="flex flex-col items-center gap-3">
            <WebcamCapture onCapture={setPhoto} />
            {!photo && (
              <p className="text-sm text-muted-foreground">
                A photo is required to submit.
              </p>
            )}
          </CardContent>
        </Card>

        <Separator />

        {serverError && (
          <p className="text-sm text-destructive text-center">{serverError}</p>
        )}

        <Button
          type="submit"
          disabled={isSubmitting || !photo}
          className="w-full"
        >
          {isSubmitting ? 'Submitting…' : 'Submit Onboarding'}
        </Button>
      </div>
    </form>
  )
}
