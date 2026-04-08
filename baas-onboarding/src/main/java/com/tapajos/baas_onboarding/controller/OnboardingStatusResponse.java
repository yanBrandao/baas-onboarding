package com.tapajos.baas_onboarding.controller;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OnboardingStatusResponse(
        @JsonProperty("onboarding_id") String onboardingId,
        String status
) {}
