package com.tapajos.baas_account.infrastructure.config;

import com.tapajos.baas.common.kafka.OnboardingEventPublisher;
import com.tapajos.baas_account.infrastructure.kafka.KafkaEventPublisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
public class KafkaPublisherConfig {

    @Value("${baas.kafka.topic}")
    private String topic;

    @Bean
    public OnboardingEventPublisher onboardingEventPublisher(KafkaTemplate<String, String> kafkaTemplate) {
        return new KafkaEventPublisher(kafkaTemplate, topic);
    }
}
