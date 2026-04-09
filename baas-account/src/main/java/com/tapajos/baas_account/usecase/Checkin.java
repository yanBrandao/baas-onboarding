package com.tapajos.baas_account.usecase;

import com.tapajos.baas_account.domain.Account;
import com.tapajos.baas_account.domain.AccountStatus;
import com.tapajos.baas_account.domain.Currency;
import com.tapajos.baas_account.domain.Transaction;
import com.tapajos.baas_account.domain.TransactionType;
import com.tapajos.baas_account.exception.AccountBlockedException;
import com.tapajos.baas_account.exception.AccountNotFoundException;
import com.tapajos.baas_account.exception.ConcurrentModificationException;
import com.tapajos.baas_account.exception.CurrencyMismatchException;
import com.tapajos.baas_account.repository.AccountRepository;
import com.tapajos.baas_account.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;

import java.math.BigDecimal;
import java.time.Instant;

@Service
public class Checkin {

    private static final int MAX_RETRIES = 3;

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public Checkin(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    public Transaction execute(String accountId, BigDecimal amount, Currency currency, String idempotencyKey) {
        // Idempotency check — transaction_id IS the idempotency key
        var existing = transactionRepository.findById(accountId, idempotencyKey);
        if (existing.isPresent()) {
            return existing.get();
        }

        for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
            Account account = accountRepository.findById(accountId)
                    .orElseThrow(() -> new AccountNotFoundException(accountId));

            if (account.status() == AccountStatus.BLOCKED) {
                throw new AccountBlockedException(accountId);
            }
            if (account.currency() != currency) {
                throw new CurrencyMismatchException(account.currency().name(), currency.name());
            }

            Transaction tx = new Transaction(
                    idempotencyKey,
                    accountId,
                    TransactionType.CHECKIN,
                    amount,
                    currency,
                    Instant.now().toString()
            );

            try {
                // Save transaction first with idempotency condition
                transactionRepository.save(tx);
                // Update balance with OCC — ConditionalCheckFailedException retries the loop
                accountRepository.updateBalance(account, account.balance().add(amount));
                return tx;
            } catch (ConditionalCheckFailedException e) {
                // Transaction already saved (race on idempotency) → return existing
                var race = transactionRepository.findById(accountId, idempotencyKey);
                if (race.isPresent()) return race.get();
                // Otherwise it was the balance OCC that failed → retry
            }
        }

        throw new ConcurrentModificationException(accountId);
    }
}
