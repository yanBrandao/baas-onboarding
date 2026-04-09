package com.tapajos.baas.common.kafka;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("baas.kafka")
public class OnboardingKafkaProperties {

    private String bootstrapServers = "localhost:9092";
    private String topic = "baas-onboarding";

    public String getBootstrapServers() {
        return bootstrapServers;
    }

    public void setBootstrapServers(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }
}
