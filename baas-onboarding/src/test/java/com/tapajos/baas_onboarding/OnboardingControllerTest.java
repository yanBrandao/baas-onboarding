package com.tapajos.baas_onboarding;

import com.tapajos.baas_onboarding.controller.OnboardingController;
import com.tapajos.baas_onboarding.domain.Address;
import com.tapajos.baas_onboarding.domain.Onboarding;
import com.tapajos.baas_onboarding.usecase.GetOnboardingDetails;
import com.tapajos.baas_onboarding.usecase.OnboardingNewCustomer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OnboardingController.class)
class OnboardingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OnboardingNewCustomer onboardingNewCustomer;

    @MockitoBean
    private GetOnboardingDetails getOnboardingDetails;

    @Test
    void shouldCreateOnboardingAndReturnId() throws Exception {
        when(onboardingNewCustomer.execute(any())).thenReturn("generated-id-123");

        mockMvc.perform(post("/onboarding")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "John Doe",
                                  "email": "johndoe@domain.com",
                                  "phone": "1234567890",
                                  "document": "1234567890",
                                  "birth_date": "1990-01-01",
                                  "mother_name": "Jane Doe",
                                  "fingerprint": "base64_fingerprint",
                                  "address": {
                                    "street": "123 Main St",
                                    "city": "New York",
                                    "state": "NY",
                                    "zip": "12345"
                                  }
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.onboarding_id").value("generated-id-123"));
    }

    @Test
    void shouldGetOnboardingById() throws Exception {
        Onboarding onboarding = new Onboarding(
                "existing-id", "IN_PROGRESS", "John Doe",
                "johndoe@domain.com", "1234567890", "1234567890",
                "1990-01-01", "Jane Doe", "base64",
                new Address("123 Main St", "New York", "NY", "12345")
        );
        when(getOnboardingDetails.execute(eq("existing-id"))).thenReturn(Optional.of(onboarding));

        mockMvc.perform(get("/onboarding/existing-id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.onboarding_id").value("existing-id"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    void shouldGetOnboardingStatus() throws Exception {
        Onboarding onboarding = new Onboarding(
                "existing-id", "IN_PROGRESS", "John Doe",
                "johndoe@domain.com", "1234567890", "1234567890",
                "1990-01-01", "Jane Doe", "base64",
                new Address("123 Main St", "New York", "NY", "12345")
        );
        when(getOnboardingDetails.execute(eq("existing-id"))).thenReturn(Optional.of(onboarding));

        mockMvc.perform(get("/onboarding/existing-id/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.onboarding_id").value("existing-id"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void shouldReturn404WhenOnboardingNotFoundById() throws Exception {
        when(getOnboardingDetails.execute(eq("unknown-id"))).thenReturn(Optional.empty());

        mockMvc.perform(get("/onboarding/unknown-id"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn404WhenOnboardingStatusNotFound() throws Exception {
        when(getOnboardingDetails.execute(eq("unknown-id"))).thenReturn(Optional.empty());

        mockMvc.perform(get("/onboarding/unknown-id/status"))
                .andExpect(status().isNotFound());
    }
}
