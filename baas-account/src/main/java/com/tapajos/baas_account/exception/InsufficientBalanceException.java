package com.tapajos.baas_account.exception;

import java.math.BigDecimal;

public class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException(BigDecimal balance, BigDecimal requested) {
        super("Insufficient balance: available=" + balance + ", requested=" + requested);
    }
}
