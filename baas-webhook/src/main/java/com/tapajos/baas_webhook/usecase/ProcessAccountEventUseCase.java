package com.tapajos.baas_webhook.usecase;

import com.tapajos.baas_webhook.domain.AccountEventPayload;
import com.tapajos.baas_webhook.infrastructure.client.OnboardingApiClient;
import com.tapajos.baas_webhook.infrastructure.service.CustomerNotificationService;
import org.springframework.stereotype.Service;

@Service
public class ProcessAccountEventUseCase {

    private final CustomerNotificationService notificationService;
    private final OnboardingApiClient onboardingApiClient;

    public ProcessAccountEventUseCase(CustomerNotificationService notificationService,
                                      OnboardingApiClient onboardingApiClient) {
        this.notificationService = notificationService;
        this.onboardingApiClient = onboardingApiClient;
    }

    public void execute(AccountEventPayload payload) {
        // 1. Send the email notification
        notificationService.sendEmailNotification(
                payload.customerEmail(), 
                payload.onboardingId(), 
                payload.status()
        );

        // 2. Update the tracking state in the onboarding microservice
        onboardingApiClient.updateStatus(payload.onboardingId(), payload.status());
    }
}
