package com.tapajos.baas.common.message;

public record StepResult(
    StepStatus status,
    String message
) {
    public static StepResult pending() {
        return new StepResult(StepStatus.PENDING, "");
    }

    public static StepResult completed(String message) {
        return new StepResult(StepStatus.COMPLETED, message);
    }

    public static StepResult failed(String message) {
        return new StepResult(StepStatus.FAILED, message);
    }
}
