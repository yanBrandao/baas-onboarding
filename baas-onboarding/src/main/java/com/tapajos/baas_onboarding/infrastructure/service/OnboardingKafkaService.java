package com.tapajos.baas_onboarding.infrastructure.service;

import com.tapajos.baas.common.kafka.OnboardingEventPublisher;
import com.tapajos.baas.common.message.OnboardingAddress;
import com.tapajos.baas.common.message.OnboardingData;
import com.tapajos.baas.common.message.OnboardingMessage;
import com.tapajos.baas.common.message.OnboardingMetadata;
import com.tapajos.baas.common.message.OnboardingStep;
import com.tapajos.baas_onboarding.domain.Onboarding;
import org.springframework.stereotype.Service;

@Service
public class OnboardingKafkaService {

    private final OnboardingEventPublisher publisher;

    public OnboardingKafkaService(OnboardingEventPublisher publisher) {
        this.publisher = publisher;
    }

    public void send(Onboarding onboarding) {
        OnboardingAddress address = null;
        if (onboarding.address() != null) {
            address = new OnboardingAddress(
                    onboarding.address().street(),
                    onboarding.address().city(),
                    onboarding.address().state(),
                    onboarding.address().zip()
            );
        }

        OnboardingData data = new OnboardingData(
                onboarding.name(),
                onboarding.email(),
                onboarding.phone(),
                onboarding.document(),
                onboarding.birthDate(),
                onboarding.motherName(),
                onboarding.fingerprint(),
                address
        );

        OnboardingMessage message = OnboardingMessage.of(
                onboarding.onboardingId(),
                OnboardingStep.FRAUD_CHECK,
                data,
                OnboardingMetadata.initial()
        );

        publisher.publish(message);
    }
}
