package com.tapajos.baas_frauds;

import com.tapajos.baas.common.kafka.OnboardingEventPublisher;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=localhost:9092",
        "baas.kafka.bootstrap-servers=localhost:9092",
        "baas.kafka.topic=baas-onboarding"
})
class BaasFraudsApplicationTests {

    @MockitoBean
    OnboardingEventPublisher onboardingEventPublisher;

    @Test
    void contextLoads() {
    }
}
