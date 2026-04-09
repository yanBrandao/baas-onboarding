package com.tapajos.baas_frauds.infrastructure.sqs;

import com.tapajos.baas.common.message.OnboardingError;
import com.tapajos.baas.common.message.OnboardingMessage;
import com.tapajos.baas.common.message.OnboardingMetadata;
import com.tapajos.baas.common.sns.OnboardingSnsPublisher;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class FraudDetectionListener {

    private static final Logger logger = LoggerFactory.getLogger(FraudDetectionListener.class);
    private final OnboardingSnsPublisher snsPublisher;

    public FraudDetectionListener(OnboardingSnsPublisher snsPublisher) {
        this.snsPublisher = snsPublisher;
    }

    @SqsListener("${baas.sqs.queue-name:baas-fraud-queue}")
    public void onMessage(OnboardingMessage message) {
        logger.info("Received onboarding message for fraud check: [onboarding_id={}]", message.onboardingId());

        try {
            // Simulate fraud detection logic
            boolean isFraud = simulateFraudCheck(message);

            if (isFraud) {
                logger.warn("Fraud detected for onboarding [onboarding_id={}]", message.onboardingId());
                sendErrorToSns(message, "FRAUD_DETECTED", "High risk fraud profile detected");
            } else {
                logger.info("Fraud check passed for onboarding [onboarding_id={}]", message.onboardingId());
                // In a real scenario, we would publish a SUCCESS event to move to the next step
            }
        } catch (Exception e) {
            logger.error("Error processing fraud check for onboarding [onboarding_id={}]", message.onboardingId(), e);
            sendErrorToSns(message, "INTERNAL_ERROR", "Error during fraud detection processing");
        }
    }

    private boolean simulateFraudCheck(OnboardingMessage message) {
        // Simple logic: if name contains "fraud", mark as fraud
        return message.data().name().toLowerCase().contains("fraud");
    }

    private void sendErrorToSns(OnboardingMessage message, String errorCode, String errorMessage) {
        logger.info("Sending error report to SNS [onboarding_id={}, code={}]", message.onboardingId(), errorCode);
        
        OnboardingError error = new OnboardingError(errorCode, errorMessage);
        OnboardingMetadata updatedMetadata = message.metadata().withError(error);
        OnboardingMessage errorMessageToPublish = message.withMetadata(updatedMetadata);
        
        snsPublisher.publish(errorMessageToPublish); 
    }
}
