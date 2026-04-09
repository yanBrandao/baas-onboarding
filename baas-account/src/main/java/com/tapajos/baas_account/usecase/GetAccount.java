package com.tapajos.baas_account.usecase;

import com.tapajos.baas_account.domain.Account;
import com.tapajos.baas_account.exception.AccountNotFoundException;
import com.tapajos.baas_account.repository.AccountRepository;
import org.springframework.stereotype.Service;

@Service
public class GetAccount {

    private final AccountRepository accountRepository;

    public GetAccount(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Account execute(String accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
    }
}
