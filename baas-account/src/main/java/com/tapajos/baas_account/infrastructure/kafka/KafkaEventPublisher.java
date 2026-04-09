package com.tapajos.baas_account.infrastructure.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tapajos.baas.common.kafka.OnboardingEventPublisher;
import com.tapajos.baas.common.message.OnboardingMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;

public class KafkaEventPublisher implements OnboardingEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(KafkaEventPublisher.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String topic;

    public KafkaEventPublisher(KafkaTemplate<String, String> kafkaTemplate, String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @Override
    public void publish(OnboardingMessage message) {
        try {
            String payload = OBJECT_MAPPER.writeValueAsString(message);
            logger.info("Publishing Kafka message [onboarding_id={}, step={}]",
                    message.onboardingId(), message.step());
            kafkaTemplate.send(topic, message.onboardingId(), payload);
            logger.info("Kafka message published [onboarding_id={}, step={}]",
                    message.onboardingId(), message.step());
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize onboarding message [onboarding_id={}]",
                    message.onboardingId(), e);
            throw new RuntimeException("Failed to serialize onboarding message", e);
        }
    }
}
