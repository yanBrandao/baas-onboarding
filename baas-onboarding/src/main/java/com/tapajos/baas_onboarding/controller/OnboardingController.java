package com.tapajos.baas_onboarding.controller;

import com.tapajos.baas_onboarding.domain.Onboarding;
import com.tapajos.baas_onboarding.usecase.GetOnboardingDetails;
import com.tapajos.baas_onboarding.usecase.OnboardingNewCustomer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/onboarding")
public class OnboardingController {

    private final OnboardingNewCustomer onboardingNewCustomer;
    private final GetOnboardingDetails getOnboardingDetails;

    public OnboardingController(OnboardingNewCustomer onboardingNewCustomer,
                                GetOnboardingDetails getOnboardingDetails) {
        this.onboardingNewCustomer = onboardingNewCustomer;
        this.getOnboardingDetails = getOnboardingDetails;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> createOnboarding(@RequestBody OnboardingRequest request) {
        String onboardingId = onboardingNewCustomer.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("onboarding_id", onboardingId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Onboarding> getOnboarding(@PathVariable String id) {
        return getOnboardingDetails.execute(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<OnboardingStatusResponse> getOnboardingStatus(@PathVariable String id) {
        return getOnboardingDetails.execute(id)
                .map(o -> ResponseEntity.ok(new OnboardingStatusResponse(o.onboardingId(), o.status())))
                .orElse(ResponseEntity.notFound().build());
    }
}
