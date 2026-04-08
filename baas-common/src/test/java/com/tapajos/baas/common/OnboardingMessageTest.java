package com.tapajos.baas.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.tapajos.baas.common.message.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OnboardingMessageTest {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

    @Test
    void shouldDeriveNextStepsFromStep() {
        OnboardingMessage msg = OnboardingMessage.of(
                "abc-123",
                OnboardingStep.FRAUD_CHECK,
                sampleData(),
                OnboardingMetadata.initial()
        );

        assertEquals(OnboardingStep.FRAUD_CHECK, msg.step());
        assertEquals(
                List.of(OnboardingStep.CUSTOMER_CREATION, OnboardingStep.ACCOUNT_CREATION, OnboardingStep.ACCOUNT_NOTIFICATION),
                msg.nextSteps()
        );
    }

    @Test
    void shouldSerializeToExpectedJsonShape() throws Exception {
        OnboardingMessage msg = OnboardingMessage.of(
                "abc-123",
                OnboardingStep.FRAUD_CHECK,
                sampleData(),
                OnboardingMetadata.initial()
        );

        String json = objectMapper.writeValueAsString(msg);
        JsonNode root = objectMapper.readTree(json);

        assertEquals("abc-123", root.get("onboarding_id").asText());
        assertEquals("FRAUD_CHECK", root.get("step").asText());
        assertTrue(root.get("next_steps").isArray());
        assertEquals(3, root.get("next_steps").size());
        assertNotNull(root.get("data"));
        assertNotNull(root.get("metadata"));
        assertEquals("IN_PROGRESS", root.get("metadata").get("status").asText());
        assertEquals("PENDING", root.get("metadata").get("fraud_check").get("status").asText());
    }

    @Test
    void shouldHaveEmptyNextStepsForLastStep() {
        assertEquals(List.of(), OnboardingStep.ACCOUNT_NOTIFICATION.nextSteps());
    }

    private OnboardingData sampleData() {
        return new OnboardingData(
                "John Doe", "john@example.com", "123456789",
                "DOC-001", "1990-01-01", "Jane Doe",
                "fingerprint==",
                new OnboardingAddress("123 Main St", "New York", "NY", "12345")
        );
    }
}
