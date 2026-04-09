package com.tapajos.baas_account.domain;

import java.math.BigDecimal;

public record Account(
    String accountId,
    String onboardingId,
    Currency currency,
    BigDecimal balance,
    AccountStatus status,
    Long version,
    String createdAt
) {}
