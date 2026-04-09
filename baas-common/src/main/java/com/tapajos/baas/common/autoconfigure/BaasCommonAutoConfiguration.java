package com.tapajos.baas.common.autoconfigure;

import com.tapajos.baas.common.kafka.OnboardingEventPublisher;
import com.tapajos.baas.common.kafka.OnboardingKafkaProperties;
import com.tapajos.baas.common.kafka.OnboardingKafkaPublisher;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.HashMap;
import java.util.Map;

@AutoConfiguration
@EnableConfigurationProperties(OnboardingKafkaProperties.class)
public class BaasCommonAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public OnboardingEventPublisher onboardingEventPublisher(OnboardingKafkaProperties props) {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, props.getBootstrapServers());
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        var template = new KafkaTemplate<>(new DefaultKafkaProducerFactory<String, String>(config));
        return new OnboardingKafkaPublisher(template, props.getTopic());
    }
}
