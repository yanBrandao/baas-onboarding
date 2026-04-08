package com.tapajos.baas.common.message;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public record OnboardingMetadata(
    @JsonProperty("fraud_check")       StepResult fraudCheck,
    @JsonProperty("customer_creation") StepResult customerCreation,
    @JsonProperty("account_creation")  StepResult accountCreation,
    OnboardingError error,
    OnboardingStatus status,
    @JsonProperty("created_at") String createdAt,
    @JsonProperty("updated_at") String updatedAt
) {
    /**
     * Initial metadata for a brand-new onboarding — all steps pending.
     */
    public static OnboardingMetadata initial() {
        String now = Instant.now().toString();
        return new OnboardingMetadata(
            StepResult.pending(),
            StepResult.pending(),
            StepResult.pending(),
            OnboardingError.empty(),
            OnboardingStatus.IN_PROGRESS,
            now,
            now
        );
    }

    /**
     * Returns a copy with the updated_at timestamp refreshed to now.
     */
    public OnboardingMetadata withUpdatedAt() {
        return new OnboardingMetadata(
            fraudCheck, customerCreation, accountCreation,
            error, status, createdAt, Instant.now().toString()
        );
    }
}
