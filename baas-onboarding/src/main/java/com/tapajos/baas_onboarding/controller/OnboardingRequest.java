package com.tapajos.baas_onboarding.controller;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OnboardingRequest(
        String name,
        String email,
        String phone,
        String document,
        @JsonProperty("birth_date") String birthDate,
        @JsonProperty("mother_name") String motherName,
        String fingerprint,
        AddressRequest address
) {}
