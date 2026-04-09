package com.tapajos.baas_onboarding;

import com.tapajos.baas.common.sns.OnboardingSnsPublisher;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@TestPropertySource(properties = {
        "amazon.dynamodb.endpoint=http://localhost:4566",
        "amazon.aws.region=us-east-1",
        "baas.sns.endpoint=http://localhost:4566",
        "baas.sns.topic-arn=arn:aws:sns:us-east-1:000000000000:baas-onboarding",
        "baas.sns.region=us-east-1"
})
class BaasOnboardingApplicationTests {

    @MockitoBean
    OnboardingSnsPublisher onboardingSnsPublisher;

    @Test
    void contextLoads() {
    }
}
