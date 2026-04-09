package com.tapajos.baas_account.usecase;

import com.tapajos.baas_account.domain.Account;
import com.tapajos.baas_account.domain.AccountStatus;
import com.tapajos.baas_account.exception.AccountNotFoundException;
import com.tapajos.baas_account.repository.AccountRepository;
import org.springframework.stereotype.Service;

@Service
public class BlockAccount {

    private final AccountRepository accountRepository;

    public BlockAccount(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Account execute(String accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
        return accountRepository.updateStatus(accountId, AccountStatus.BLOCKED.name(), account.version())
                .orElseThrow(() -> new AccountNotFoundException(accountId));
    }
}
