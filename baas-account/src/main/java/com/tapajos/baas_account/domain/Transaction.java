package com.tapajos.baas_account.domain;

import java.math.BigDecimal;

public record Transaction(
    String transactionId,
    String accountId,
    TransactionType type,
    BigDecimal amount,
    Currency currency,
    String createdAt
) {}
