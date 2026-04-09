package com.tapajos.baas_account.exception;

public class AccountBlockedException extends RuntimeException {
    public AccountBlockedException(String accountId) {
        super("Account is blocked: " + accountId);
    }
}
