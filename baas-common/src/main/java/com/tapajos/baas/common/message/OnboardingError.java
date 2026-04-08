package com.tapajos.baas.common.message;

public record OnboardingError(
    String code,
    String message
) {
    public static OnboardingError empty() {
        return new OnboardingError("", "");
    }
}
