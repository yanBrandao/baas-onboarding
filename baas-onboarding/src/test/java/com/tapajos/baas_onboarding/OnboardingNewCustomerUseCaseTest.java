package com.tapajos.baas_onboarding;

import com.tapajos.baas_onboarding.controller.AddressRequest;
import com.tapajos.baas_onboarding.controller.OnboardingRequest;
import com.tapajos.baas_onboarding.domain.Onboarding;
import com.tapajos.baas_onboarding.infrastructure.service.OnboardingSnsService;
import com.tapajos.baas_onboarding.repository.OnboardingRepository;
import com.tapajos.baas_onboarding.usecase.OnboardingNewCustomer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;

@ExtendWith(MockitoExtension.class)
class OnboardingNewCustomerUseCaseTest {

    @Mock
    private OnboardingRepository repository;

    @Mock
    private OnboardingSnsService snsService;

    @InjectMocks
    private OnboardingNewCustomer useCase;

    @Test
    void shouldSendSnsBeforeSavingToDynamo() {
        OnboardingRequest request = buildRequest();

        useCase.execute(request);

        InOrder inOrder = inOrder(snsService, repository);
        inOrder.verify(snsService).send(any(Onboarding.class));
        inOrder.verify(repository).save(any(Onboarding.class));
    }

    @Test
    void shouldReturnGeneratedOnboardingId() {
        OnboardingRequest request = buildRequest();

        String onboardingId = useCase.execute(request);

        assertThat(onboardingId).isNotNull().isNotEmpty();
    }

    private OnboardingRequest buildRequest() {
        AddressRequest address = new AddressRequest("123 Main St", "New York", "NY", "12345");
        return new OnboardingRequest(
                "John Doe", "johndoe@domain.com", "1234567890",
                "1234567890", "1990-01-01", "Jane Doe",
                "base64_fingerprint", address
        );
    }
}
