package com.tapajos.baas_onboarding.controller;

public record AddressRequest(
        String street,
        String city,
        String state,
        String zip
) {}
