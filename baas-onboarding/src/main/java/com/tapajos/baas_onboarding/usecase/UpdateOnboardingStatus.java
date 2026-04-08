package com.tapajos.baas_onboarding.usecase;

import com.tapajos.baas_onboarding.domain.Onboarding;
import com.tapajos.baas_onboarding.repository.OnboardingRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UpdateOnboardingStatus {

    private final OnboardingRepository repository;

    public UpdateOnboardingStatus(OnboardingRepository repository) {
        this.repository = repository;
    }

    public Optional<Onboarding> execute(String onboardingId, String status) {
        return repository.updateStatus(onboardingId, status);
    }
}
