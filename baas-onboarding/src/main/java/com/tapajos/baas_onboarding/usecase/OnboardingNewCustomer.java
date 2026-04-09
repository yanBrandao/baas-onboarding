package com.tapajos.baas_onboarding.usecase;

import com.tapajos.baas_onboarding.controller.OnboardingRequest;
import com.tapajos.baas_onboarding.domain.Address;
import com.tapajos.baas_onboarding.domain.Onboarding;
import com.tapajos.baas_onboarding.infrastructure.service.OnboardingKafkaService;
import com.tapajos.baas_onboarding.repository.OnboardingRepository;
import org.springframework.stereotype.Component;

@Component
public class OnboardingNewCustomer {

    private final OnboardingRepository repository;
    private final OnboardingKafkaService kafkaService;

    public OnboardingNewCustomer(OnboardingRepository repository, OnboardingKafkaService kafkaService) {
        this.repository = repository;
        this.kafkaService = kafkaService;
    }

    public String execute(OnboardingRequest request) {
        Address address = null;
        if (request.address() != null) {
            address = new Address(
                    request.address().street(),
                    request.address().city(),
                    request.address().state(),
                    request.address().zip()
            );
        }

        Onboarding onboarding = new Onboarding(
                null,
                null,
                request.name(),
                request.email(),
                request.phone(),
                request.document(),
                request.birthDate(),
                request.motherName(),
                request.fingerprint(),
                address
        );

        kafkaService.send(onboarding);
        repository.save(onboarding);

        return onboarding.onboardingId();
    }
}
