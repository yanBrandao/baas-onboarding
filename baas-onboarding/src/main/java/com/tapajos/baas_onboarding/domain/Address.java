package com.tapajos.baas_onboarding.domain;

public record Address(
        String street,
        String city,
        String state,
        String zip
) {}
