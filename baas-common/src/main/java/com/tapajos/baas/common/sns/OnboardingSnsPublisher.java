package com.tapajos.baas.common.sns;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tapajos.baas.common.message.OnboardingMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

public class OnboardingSnsPublisher {

    private static final Logger logger = LoggerFactory.getLogger(OnboardingSnsPublisher.class);

    private final SnsClient snsClient;
    private final ObjectMapper objectMapper;
    private final String topicArn;

    public OnboardingSnsPublisher(SnsClient snsClient, ObjectMapper objectMapper, String topicArn) {
        this.snsClient = snsClient;
        this.objectMapper = objectMapper;
        this.topicArn = topicArn;
    }

    /**
     * Serializes {@code message} to JSON and publishes it to the configured SNS topic.
     */
    public void publish(OnboardingMessage message) {
        try {
            String payload = objectMapper.writeValueAsString(message);
            logger.info("Publishing SNS message [onboarding_id={}, step={}]",
                    message.onboardingId(), message.step());

            snsClient.publish(PublishRequest.builder()
                    .topicArn(topicArn)
                    .message(payload)
                    .build());

            logger.info("SNS message published [onboarding_id={}, step={}]",
                    message.onboardingId(), message.step());
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize onboarding message [onboarding_id={}]",
                    message.onboardingId(), e);
            throw new RuntimeException("Failed to serialize onboarding message", e);
        } catch (Exception e) {
            logger.error("Failed to publish SNS message [onboarding_id={}, step={}]",
                    message.onboardingId(), message.step(), e);
            throw new RuntimeException("Failed to publish onboarding message to SNS", e);
        }
    }
}
