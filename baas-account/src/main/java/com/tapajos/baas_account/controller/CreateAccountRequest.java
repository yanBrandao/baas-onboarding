package com.tapajos.baas_account.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tapajos.baas_account.domain.Currency;

public record CreateAccountRequest(
        @JsonProperty("onboarding_id") String onboardingId,
        Currency currency
) {}
