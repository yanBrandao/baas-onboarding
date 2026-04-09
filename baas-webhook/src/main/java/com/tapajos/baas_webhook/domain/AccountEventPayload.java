package com.tapajos.baas_webhook.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AccountEventPayload(
    @JsonProperty("onboarding_id") String onboardingId,
    @JsonProperty("status") String status,
    @JsonProperty("customer_email") String customerEmail
) {}
