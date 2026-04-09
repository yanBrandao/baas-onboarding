package com.tapajos.baas_account.controller;

import com.tapajos.baas_account.domain.Currency;

import java.math.BigDecimal;

public record TransactionRequest(
        BigDecimal amount,
        Currency currency
) {}
