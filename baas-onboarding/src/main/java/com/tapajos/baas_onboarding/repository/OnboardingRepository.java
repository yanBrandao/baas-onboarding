package com.tapajos.baas_onboarding.repository;

import com.tapajos.baas_onboarding.domain.Onboarding;

import java.util.Optional;

public interface OnboardingRepository {
    void save(Onboarding onboarding);
    Optional<Onboarding> findById(String onboardingId);
    Optional<Onboarding> updateStatus(String onboardingId, String status);
}
