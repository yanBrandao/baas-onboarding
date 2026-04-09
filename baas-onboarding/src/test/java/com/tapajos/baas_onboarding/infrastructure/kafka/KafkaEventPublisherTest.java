package com.tapajos.baas_onboarding.infrastructure.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tapajos.baas.common.message.OnboardingAddress;
import com.tapajos.baas.common.message.OnboardingData;
import com.tapajos.baas.common.message.OnboardingMessage;
import com.tapajos.baas.common.message.OnboardingMetadata;
import com.tapajos.baas.common.message.OnboardingStep;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class KafkaEventPublisherTest {

    private static final String TOPIC = "baas-frauds";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    private KafkaEventPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new KafkaEventPublisher(kafkaTemplate, TOPIC);
    }

    @Test
    void shouldSendToCorrectTopicWithOnboardingIdAsKey() {
        publisher.publish(buildMessage("onboarding-123"));

        verify(kafkaTemplate).send(eq(TOPIC), eq("onboarding-123"), anyString());
    }

    @Test
    void shouldSerializeMessageAsJsonWithExpectedFields() throws Exception {
        publisher.publish(buildMessage("onboarding-456"));

        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(anyString(), anyString(), payloadCaptor.capture());

        JsonNode root = OBJECT_MAPPER.readTree(payloadCaptor.getValue());
        assertThat(root.get("onboarding_id").asText()).isEqualTo("onboarding-456");
        assertThat(root.get("step").asText()).isEqualTo("FRAUD_CHECK");
        assertThat(root.has("data")).isTrue();
        assertThat(root.has("metadata")).isTrue();
    }

    @Test
    void shouldSendExactlyOncePerPublishCall() {
        publisher.publish(buildMessage("onboarding-789"));

        verify(kafkaTemplate, times(1)).send(anyString(), anyString(), anyString());
    }

    private OnboardingMessage buildMessage(String onboardingId) {
        OnboardingData data = new OnboardingData(
                "John Doe", "john@example.com", "1234567890",
                "DOC-001", "1990-01-01", "Jane Doe", "fingerprint==",
                new OnboardingAddress("123 Main St", "New York", "NY", "12345")
        );
        return OnboardingMessage.of(onboardingId, OnboardingStep.FRAUD_CHECK, data, OnboardingMetadata.initial());
    }
}
