package com.tapajos.baas_account.exception;

public class ConcurrentModificationException extends RuntimeException {
    public ConcurrentModificationException(String accountId) {
        super("Concurrent modification conflict on account: " + accountId);
    }
}
