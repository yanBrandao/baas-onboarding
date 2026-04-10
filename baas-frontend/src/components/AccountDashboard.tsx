import { useEffect, useState, useCallback } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Separator } from '@/components/ui/separator'
import {
  User, CreditCard, ArrowDownCircle, ArrowUpCircle,
  RefreshCw, Loader2, CheckCircle2, AlertCircle
} from 'lucide-react'
import {
  type Onboarding, type Account, type Transaction,
  getOnboarding, getAccountByOnboardingId, getTransactions,
  searchOnboarding, checkin, checkout
} from '@/lib/api'

// ── helpers ───────────────────────────────────────────────────────────────────

function formatBRL(amount: number) {
  return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(amount)
}

function formatDate(iso: string) {
  return new Date(iso).toLocaleString('pt-BR', {
    day: '2-digit', month: '2-digit', year: 'numeric',
    hour: '2-digit', minute: '2-digit',
  })
}

function uuid() {
  return crypto.randomUUID()
}

// ── transfer form schema ──────────────────────────────────────────────────────

const transferSchema = z.object({
  recipientType: z.enum(['email', 'phone', 'document']),
  recipientValue: z.string().min(1, 'Required'),
  amount: z.string().min(1, 'Required').refine(v => !isNaN(Number(v)) && Number(v) > 0, {
    message: 'Enter a positive amount',
  }),
})

type TransferValues = z.infer<typeof transferSchema>

// ── props ─────────────────────────────────────────────────────────────────────

interface Props {
  onboardingId: string
  onReset: () => void
}

// ── component ─────────────────────────────────────────────────────────────────

export function AccountDashboard({ onboardingId, onReset }: Props) {
  const [onboarding, setOnboarding] = useState<Onboarding | null>(null)
  const [account, setAccount] = useState<Account | null>(null)
  const [transactions, setTransactions] = useState<Transaction[]>([])
  const [loadingData, setLoadingData] = useState(true)
  const [loadError, setLoadError] = useState<string | null>(null)

  const [transferStatus, setTransferStatus] = useState<
    'idle' | 'loading' | 'success' | 'error'
  >('idle')
  const [transferMessage, setTransferMessage] = useState<string | null>(null)

  const {
    register,
    handleSubmit,
    formState: { errors },
    reset: resetForm,
  } = useForm<TransferValues>({
    resolver: zodResolver(transferSchema),
    defaultValues: { recipientType: 'email' },
  })

  const loadTransactions = useCallback(async (accountId: string) => {
    const txs = await getTransactions(accountId)
    setTransactions(txs)
  }, [])

  useEffect(() => {
    async function load() {
      try {
        const [ob, acc] = await Promise.all([
          getOnboarding(onboardingId),
          getAccountByOnboardingId(onboardingId),
        ])
        setOnboarding(ob)
        setAccount(acc)
        await loadTransactions(acc.account_id)
      } catch (e) {
        setLoadError(e instanceof Error ? e.message : 'Failed to load account data')
      } finally {
        setLoadingData(false)
      }
    }
    load()
  }, [onboardingId, loadTransactions])

  async function onTransfer(values: TransferValues) {
    if (!account) return
    setTransferStatus('loading')
    setTransferMessage(null)

    try {
      const recipient = await searchOnboarding(values.recipientType, values.recipientValue)
      const recipientAccount = await getAccountByOnboardingId(recipient.onboarding_id)

      const amount = Number(values.amount)
      const key = uuid()

      await checkout(account.account_id, amount, key)
      await checkin(recipientAccount.account_id, amount, uuid())

      setTransferStatus('success')
      setTransferMessage(`Transferred ${formatBRL(amount)} successfully.`)
      resetForm()

      // refresh balance and history
      const [updatedAcc, txs] = await Promise.all([
        getAccountByOnboardingId(onboardingId),
        getTransactions(account.account_id),
      ])
      setAccount(updatedAcc)
      setTransactions(txs)
    } catch (e) {
      setTransferStatus('error')
      setTransferMessage(e instanceof Error ? e.message : 'Transfer failed')
    }
  }

  // ── loading / error state ─────────────────────────────────────────────────

  if (loadingData) {
    return (
      <div className="flex flex-col items-center gap-4 py-20">
        <Loader2 className="h-10 w-10 animate-spin text-primary" />
        <p className="text-muted-foreground">Loading your account…</p>
      </div>
    )
  }

  if (loadError || !onboarding || !account) {
    return (
      <div className="flex flex-col items-center gap-4 py-20 text-destructive">
        <AlertCircle className="h-10 w-10" />
        <p>{loadError ?? 'Account not found'}</p>
        <Button variant="outline" onClick={onReset}>Go back</Button>
      </div>
    )
  }

  // ── dashboard ─────────────────────────────────────────────────────────────

  return (
    <div className="flex flex-col gap-6">

      {/* Personal info */}
      <Card>
        <CardHeader className="flex flex-row items-center gap-2 pb-3">
          <User className="h-5 w-5 text-muted-foreground" />
          <CardTitle className="text-base">Personal Information</CardTitle>
        </CardHeader>
        <CardContent className="grid grid-cols-1 md:grid-cols-2 gap-3 text-sm">
          <Info label="Name" value={onboarding.name} />
          <Info label="Email" value={onboarding.email} />
          <Info label="Phone" value={onboarding.phone} />
          <Info label="Document" value={onboarding.document} />
          <Info label="Birth Date" value={onboarding.birth_date} />
          <Info label="Mother's Name" value={onboarding.mother_name} />
          {onboarding.address && (
            <div className="md:col-span-2">
              <Info
                label="Address"
                value={`${onboarding.address.street}, ${onboarding.address.city} – ${onboarding.address.state} ${onboarding.address.zip}`}
              />
            </div>
          )}
        </CardContent>
      </Card>

      {/* Account info */}
      <Card>
        <CardHeader className="flex flex-row items-center gap-2 pb-3">
          <CreditCard className="h-5 w-5 text-muted-foreground" />
          <CardTitle className="text-base">Account</CardTitle>
        </CardHeader>
        <CardContent className="flex flex-col gap-4">
          <div className="flex flex-wrap gap-4 items-end">
            <div>
              <p className="text-xs text-muted-foreground mb-1">Balance</p>
              <p className="text-3xl font-bold tracking-tight">{formatBRL(account.balance)}</p>
            </div>
            <div className="flex flex-col gap-1">
              <p className="text-xs text-muted-foreground">Status</p>
              <Badge variant={account.status === 'ACTIVE' ? 'default' : 'secondary'}>
                {account.status}
              </Badge>
            </div>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-3 text-sm">
            <Info label="Account ID" value={account.account_id} mono />
            <Info label="Currency" value={account.currency} />
            <Info label="Opened" value={formatDate(account.created_at)} />
          </div>
        </CardContent>
      </Card>

      {/* Transfer */}
      <Card>
        <CardHeader className="flex flex-row items-center gap-2 pb-3">
          <ArrowUpCircle className="h-5 w-5 text-muted-foreground" />
          <CardTitle className="text-base">Transfer</CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit(onTransfer)} noValidate className="flex flex-col gap-4">
            <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
              {/* recipient type */}
              <div className="flex flex-col gap-1.5">
                <Label htmlFor="recipientType">Find recipient by</Label>
                <select
                  id="recipientType"
                  className="flex h-9 w-full rounded-md border border-input bg-transparent px-3 py-1 text-sm shadow-sm focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring"
                  {...register('recipientType')}
                >
                  <option value="email">Email</option>
                  <option value="phone">Phone</option>
                  <option value="document">Document</option>
                </select>
              </div>

              {/* recipient value */}
              <div className="flex flex-col gap-1.5">
                <Label htmlFor="recipientValue">Recipient identifier</Label>
                <Input id="recipientValue" {...register('recipientValue')} placeholder="e.g. john@example.com" />
                {errors.recipientValue && (
                  <p className="text-xs text-destructive">{errors.recipientValue.message}</p>
                )}
              </div>

              {/* amount */}
              <div className="flex flex-col gap-1.5">
                <Label htmlFor="amount">Amount (BRL)</Label>
                <Input id="amount" type="number" step="0.01" min="0.01" {...register('amount')} placeholder="0.00" />
                {errors.amount && (
                  <p className="text-xs text-destructive">{errors.amount.message}</p>
                )}
              </div>
            </div>

            {/* feedback */}
            {transferStatus === 'success' && transferMessage && (
              <div className="flex items-center gap-2 text-sm text-green-600 animate-in fade-in">
                <CheckCircle2 className="h-4 w-4 shrink-0" />
                {transferMessage}
              </div>
            )}
            {transferStatus === 'error' && transferMessage && (
              <div className="flex items-center gap-2 text-sm text-destructive animate-in fade-in">
                <AlertCircle className="h-4 w-4 shrink-0" />
                {transferMessage}
              </div>
            )}

            <Button
              type="submit"
              disabled={transferStatus === 'loading'}
              className="w-full md:w-auto md:self-end"
            >
              {transferStatus === 'loading' ? (
                <><Loader2 className="h-4 w-4 animate-spin mr-2" />Processing…</>
              ) : 'Send Transfer'}
            </Button>
          </form>
        </CardContent>
      </Card>

      {/* Transaction history */}
      <Card>
        <CardHeader className="flex flex-row items-center justify-between pb-3">
          <div className="flex items-center gap-2">
            <RefreshCw className="h-5 w-5 text-muted-foreground" />
            <CardTitle className="text-base">Transaction History</CardTitle>
          </div>
          <Button
            variant="ghost"
            size="sm"
            onClick={() => loadTransactions(account.account_id)}
          >
            Refresh
          </Button>
        </CardHeader>
        <CardContent>
          {transactions.length === 0 ? (
            <p className="text-sm text-muted-foreground text-center py-6">No transactions yet.</p>
          ) : (
            <div className="flex flex-col divide-y">
              {transactions
                .slice()
                .sort((a, b) => new Date(b.created_at).getTime() - new Date(a.created_at).getTime())
                .map(tx => (
                  <div key={tx.transaction_id} className="flex items-center justify-between py-3">
                    <div className="flex items-center gap-3">
                      {tx.type === 'CHECKIN' ? (
                        <ArrowDownCircle className="h-5 w-5 text-green-600 shrink-0" />
                      ) : (
                        <ArrowUpCircle className="h-5 w-5 text-red-500 shrink-0" />
                      )}
                      <div>
                        <p className="text-sm font-medium">
                          {tx.type === 'CHECKIN' ? 'Cash In' : 'Cash Out'}
                        </p>
                        <p className="text-xs text-muted-foreground">{formatDate(tx.created_at)}</p>
                      </div>
                    </div>
                    <p className={`text-sm font-semibold ${tx.type === 'CHECKIN' ? 'text-green-600' : 'text-red-500'}`}>
                      {tx.type === 'CHECKIN' ? '+' : '-'}{formatBRL(tx.amount)}
                    </p>
                  </div>
                ))}
            </div>
          )}
        </CardContent>
      </Card>

      <Separator />

      <button
        onClick={onReset}
        className="text-xs text-primary underline opacity-70 hover:opacity-100 transition-opacity text-center"
      >
        Start a new onboarding
      </button>

    </div>
  )
}

// ── small helper component ────────────────────────────────────────────────────

function Info({ label, value, mono }: { label: string; value: string; mono?: boolean }) {
  return (
    <div>
      <p className="text-xs text-muted-foreground mb-0.5">{label}</p>
      <p className={`text-sm font-medium truncate ${mono ? 'font-mono text-xs' : ''}`}>{value}</p>
    </div>
  )
}
