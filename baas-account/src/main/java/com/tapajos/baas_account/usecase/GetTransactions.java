package com.tapajos.baas_account.usecase;

import com.tapajos.baas_account.domain.Transaction;
import com.tapajos.baas_account.exception.AccountNotFoundException;
import com.tapajos.baas_account.repository.AccountRepository;
import com.tapajos.baas_account.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetTransactions {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public GetTransactions(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    public List<Transaction> execute(String accountId) {
        accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
        return transactionRepository.findAllByAccountId(accountId);
    }
}
