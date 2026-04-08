package com.tapajos.baas_onboarding.domain;

import java.util.UUID;

public record Onboarding(
        String onboardingId,
        String status,
        String name,
        String email,
        String phone,
        String document,
        String birthDate,
        String motherName,
        String fingerprint,
        Address address
) {
    public Onboarding {
        if (onboardingId == null) onboardingId = UUID.randomUUID().toString();
        if (status == null) status = "IN_PROGRESS";
    }
}
