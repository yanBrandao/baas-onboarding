package com.tapajos.baas_account.infrastructure.config;

import com.tapajos.baas.common.kafka.OnboardingEventPublisher;
import com.tapajos.baas_account.infrastructure.kafka.KafkaEventPublisher;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaPublisherConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${baas.kafka.topic}")
    private String topic;

    @Bean
    public OnboardingEventPublisher onboardingEventPublisher() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        var template = new KafkaTemplate<>(new DefaultKafkaProducerFactory<String, String>(config));
        return new KafkaEventPublisher(template, topic);
    }
}
