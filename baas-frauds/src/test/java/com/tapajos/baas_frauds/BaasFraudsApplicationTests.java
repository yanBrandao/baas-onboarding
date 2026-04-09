package com.tapajos.baas_frauds;

import com.tapajos.baas.common.sns.OnboardingSnsPublisher;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.autoconfigure.exclude=io.awspring.cloud.autoconfigure.sqs.SqsAutoConfiguration",
        "baas.sns.endpoint=http://localhost:4566",
        "baas.sns.topic-arn=arn:aws:sns:us-east-1:000000000000:baas-onboarding",
        "baas.sns.region=us-east-1",
        "baas.sqs.endpoint=http://localhost:4566",
        "baas.sqs.queue-name=baas-fraud-queue",
        "baas.sqs.region=us-east-1"
})
class BaasFraudsApplicationTests {

    // Jackson 2 ObjectMapper not auto-configured in Spring Boot 4 (uses Jackson 3).
    @MockitoBean
    OnboardingSnsPublisher onboardingSnsPublisher;

    @Test
    void contextLoads() {
    }
}
