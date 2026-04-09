package com.tapajos.baas_webhook.infrastructure.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tapajos.baas.common.message.OnboardingMessage;
import com.tapajos.baas.common.message.OnboardingStep;
import com.tapajos.baas_webhook.infrastructure.client.OnboardingApiClient;
import com.tapajos.baas_webhook.infrastructure.service.CustomerNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class AccountNotificationListener {

    private static final Logger logger = LoggerFactory.getLogger(AccountNotificationListener.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final CustomerNotificationService notificationService;
    private final OnboardingApiClient onboardingApiClient;

    public AccountNotificationListener(CustomerNotificationService notificationService,
                                       OnboardingApiClient onboardingApiClient) {
        this.notificationService = notificationService;
        this.onboardingApiClient = onboardingApiClient;
    }

    @KafkaListener(topics = "${baas.kafka.consumer-topic:baas-webhook}", groupId = "${spring.kafka.consumer.group-id:baas-webhook-group}")
    public void onMessage(String payload) {
        OnboardingMessage message;
        try {
            message = OBJECT_MAPPER.readValue(payload, OnboardingMessage.class);
        } catch (Exception e) {
            logger.error("Failed to deserialize onboarding message", e);
            return;
        }

        if (message.step() != OnboardingStep.ACCOUNT_NOTIFICATION) {
            return;
        }

        logger.info("Received account notification request [onboarding_id={}]", message.onboardingId());

        try {
            notificationService.sendEmailNotification(
                    message.data().email(),
                    message.onboardingId(),
                    "COMPLETED"
            );
            onboardingApiClient.updateStatus(message.onboardingId(), "COMPLETED");
        } catch (Exception e) {
            logger.error("Failed to process account notification [onboarding_id={}]", message.onboardingId(), e);
        }
    }
}
