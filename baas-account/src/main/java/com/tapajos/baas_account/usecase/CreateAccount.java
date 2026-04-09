package com.tapajos.baas_account.usecase;

import com.tapajos.baas_account.domain.Account;
import com.tapajos.baas_account.domain.AccountStatus;
import com.tapajos.baas_account.domain.Currency;
import com.tapajos.baas_account.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
public class CreateAccount {

    private final AccountRepository accountRepository;

    public CreateAccount(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Account execute(String onboardingId, Currency currency) {
        Account account = new Account(
                UUID.randomUUID().toString(),
                onboardingId,
                currency,
                BigDecimal.ZERO,
                AccountStatus.ACTIVE,
                null,
                Instant.now().toString()
        );
        return accountRepository.save(account);
    }
}
