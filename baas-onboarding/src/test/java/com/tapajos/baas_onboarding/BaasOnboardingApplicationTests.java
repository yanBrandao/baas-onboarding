package com.tapajos.baas_onboarding;

import com.tapajos.baas.common.kafka.OnboardingEventPublisher;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@TestPropertySource(properties = {
        "amazon.dynamodb.endpoint=http://localhost:4566",
        "amazon.aws.region=us-east-1",
        "baas.kafka.bootstrap-servers=localhost:9092",
        "baas.kafka.topic=baas-onboarding"
})
class BaasOnboardingApplicationTests {

    @MockitoBean
    OnboardingEventPublisher onboardingEventPublisher;

    @Test
    void contextLoads() {
    }
}
