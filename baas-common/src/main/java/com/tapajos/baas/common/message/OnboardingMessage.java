package com.tapajos.baas.common.message;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Canonical SNS message exchanged between all BaaS microservices.
 *
 * <p>Structure (snake_case JSON via Jackson):
 * <pre>
 * {
 *   "onboarding_id": "...",
 *   "step": "FRAUD_CHECK",
 *   "next_steps": ["CUSTOMER_CREATION", "ACCOUNT_CREATION", "ACCOUNT_NOTIFICATION"],
 *   "data": { ... },
 *   "metadata": { ... }
 * }
 * </pre>
 *
 * <p>Use {@link #of(String, OnboardingStep, OnboardingData, OnboardingMetadata)} to build
 * a message for a given step; {@link OnboardingStep#nextSteps()} fills {@code next_steps}
 * automatically.
 */
public record OnboardingMessage(
    @JsonProperty("onboarding_id") String onboardingId,
    OnboardingStep step,
    @JsonProperty("next_steps") List<OnboardingStep> nextSteps,
    OnboardingData data,
    OnboardingMetadata metadata
) {
    /**
     * Builds a message for {@code step}, automatically deriving {@code next_steps}
     * from the step's position in the workflow.
     */
    public static OnboardingMessage of(
        String onboardingId,
        OnboardingStep step,
        OnboardingData data,
        OnboardingMetadata metadata
    ) {
        return new OnboardingMessage(onboardingId, step, step.nextSteps(), data, metadata);
    }

    public OnboardingMessage withMetadata(OnboardingMetadata metadata) {
        return new OnboardingMessage(onboardingId, step, nextSteps, data, metadata);
    }
}
