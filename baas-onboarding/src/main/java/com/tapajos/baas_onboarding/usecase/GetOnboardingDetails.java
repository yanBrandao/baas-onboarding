package com.tapajos.baas_onboarding.usecase;

import com.tapajos.baas_onboarding.domain.Onboarding;
import com.tapajos.baas_onboarding.repository.OnboardingRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class GetOnboardingDetails {

    private final OnboardingRepository repository;

    public GetOnboardingDetails(OnboardingRepository repository) {
        this.repository = repository;
    }

    public Optional<Onboarding> execute(String onboardingId) {
        return repository.findById(onboardingId);
    }
}
