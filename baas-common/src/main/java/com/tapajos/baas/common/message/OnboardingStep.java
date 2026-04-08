package com.tapajos.baas.common.message;

import java.util.List;

public enum OnboardingStep {

    FRAUD_CHECK,
    CUSTOMER_CREATION,
    ACCOUNT_CREATION,
    ACCOUNT_NOTIFICATION;

    /**
     * Returns the ordered list of steps that follow this one in the workflow.
     */
    public List<OnboardingStep> nextSteps() {
        OnboardingStep[] all = values();
        int current = this.ordinal();
        return List.of(all).subList(current + 1, all.length);
    }
}
