package com.tapajos.baas.common.message;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OnboardingAddress(
    String street,
    String city,
    String state,
    String zip
) {}
