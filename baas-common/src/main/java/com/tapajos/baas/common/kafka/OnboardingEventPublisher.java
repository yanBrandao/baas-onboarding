package com.tapajos.baas.common.kafka;

import com.tapajos.baas.common.message.OnboardingMessage;

public interface OnboardingEventPublisher {
    void publish(OnboardingMessage message);
}
