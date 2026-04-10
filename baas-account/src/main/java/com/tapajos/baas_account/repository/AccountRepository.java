package com.tapajos.baas_account.repository;

import com.tapajos.baas_account.domain.Account;

import java.math.BigDecimal;
import java.util.Optional;

public interface AccountRepository {
    Account save(Account account);
    Optional<Account> findById(String accountId);
    Optional<Account> updateBalance(Account account, BigDecimal newBalance);
    Optional<Account> updateStatus(String accountId, String status, Long version);
}
