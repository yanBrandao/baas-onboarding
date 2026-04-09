package com.tapajos.baas_account.repository;

import com.tapajos.baas_account.domain.Transaction;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository {
    Transaction save(Transaction transaction);
    Optional<Transaction> findById(String accountId, String transactionId);
    List<Transaction> findAllByAccountId(String accountId);
}
