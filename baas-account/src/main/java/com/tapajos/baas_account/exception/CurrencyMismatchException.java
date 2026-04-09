package com.tapajos.baas_account.exception;

public class CurrencyMismatchException extends RuntimeException {
    public CurrencyMismatchException(String accountCurrency, String requestedCurrency) {
        super("Currency mismatch: account=" + accountCurrency + ", requested=" + requestedCurrency);
    }
}
