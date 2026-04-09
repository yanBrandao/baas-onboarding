package com.tapajos.baas_frauds.infrastructure.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tapajos.baas.common.kafka.OnboardingEventPublisher;
import com.tapajos.baas.common.message.OnboardingError;
import com.tapajos.baas.common.message.OnboardingMessage;
import com.tapajos.baas.common.message.OnboardingMetadata;
import com.tapajos.baas.common.message.OnboardingStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class FraudDetectionListener {

    private static final Logger logger = LoggerFactory.getLogger(FraudDetectionListener.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final OnboardingEventPublisher eventPublisher;

    public FraudDetectionListener(OnboardingEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @KafkaListener(topics = "${baas.kafka.consumer-topic:baas-frauds}", groupId = "${spring.kafka.consumer.group-id:baas-frauds-group}")
    public void onMessage(String payload) {
        logger.info("FraudDetectionListener: Received raw payload from Kafka");
        OnboardingMessage message;
        try {
            message = OBJECT_MAPPER.readValue(payload, OnboardingMessage.class);
        } catch (Exception e) {
            logger.error("Failed to deserialize onboarding message", e);
            return;
        }

        if (message.step() != OnboardingStep.FRAUD_CHECK) {
            return;
        }

        logger.info("Received fraud check request [onboarding_id={}]", message.onboardingId());

        try {
            boolean isFraud = simulateFraudCheck(message);

            if (isFraud) {
                logger.warn("Fraud detected for onboarding [onboarding_id={}]", message.onboardingId());
                publishError(message, "FRAUD_DETECTED", "High risk fraud profile detected");
            } else {
                logger.info("Fraud check passed, advancing to ACCOUNT_CREATION [onboarding_id={}]", message.onboardingId());
                OnboardingMessage next = OnboardingMessage.of(
                        message.onboardingId(),
                        OnboardingStep.ACCOUNT_CREATION,
                        message.data(),
                        message.metadata()
                );
                eventPublisher.publish(next);
            }
        } catch (Exception e) {
            logger.error("Error processing fraud check [onboarding_id={}]", message.onboardingId(), e);
            publishError(message, "INTERNAL_ERROR", "Error during fraud detection processing");
        }
    }

    private boolean simulateFraudCheck(OnboardingMessage message) {
        // Simple simulation: if name contains "fraud", mark as fraud
        return message.data().name().toLowerCase().contains("fraud");
    }

    private void publishError(OnboardingMessage message, String errorCode, String errorMessage) {
        logger.info("Publishing error event [onboarding_id={}, code={}]", message.onboardingId(), errorCode);
        OnboardingError error = new OnboardingError(errorCode, errorMessage);
        OnboardingMetadata updatedMetadata = message.metadata().withError(error);
        eventPublisher.publish(message.withMetadata(updatedMetadata));
    }
}
