package com.tapajos.baas.common.message;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OnboardingData(
    String name,
    String email,
    String phone,
    String document,
    @JsonProperty("birth_date") String birthDate,
    @JsonProperty("mother_name") String motherName,
    String fingerprint,
    OnboardingAddress address
) {}
