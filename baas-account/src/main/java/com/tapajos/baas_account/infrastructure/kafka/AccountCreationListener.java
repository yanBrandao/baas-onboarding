package com.tapajos.baas_account.infrastructure.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tapajos.baas.common.kafka.OnboardingEventPublisher;
import com.tapajos.baas.common.message.OnboardingMessage;
import com.tapajos.baas.common.message.OnboardingStep;
import com.tapajos.baas_account.domain.Currency;
import com.tapajos.baas_account.usecase.CreateAccount;
import io.micrometer.observation.annotation.Observed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class AccountCreationListener {

    private static final Logger logger = LoggerFactory.getLogger(AccountCreationListener.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final CreateAccount createAccount;
    private final OnboardingEventPublisher eventPublisher;

    public AccountCreationListener(CreateAccount createAccount, OnboardingEventPublisher eventPublisher) {
        this.createAccount = createAccount;
        this.eventPublisher = eventPublisher;
    }

    @KafkaListener(topics = "${baas.kafka.consumer-topic:baas-account}", groupId = "${spring.kafka.consumer.group-id:baas-account-group}")
    @Observed(
            name = "baas.account.consume",
            contextualName = "process account creation event",
            lowCardinalityKeyValues = {"messaging.system", "kafka", "messaging.operation", "process"}
    )
    public void onMessage(String payload) {
        OnboardingMessage message;
        try {
            message = OBJECT_MAPPER.readValue(payload, OnboardingMessage.class);
        } catch (Exception e) {
            logger.error("Failed to deserialize onboarding message", e);
            return;
        }

        if (message.step() != OnboardingStep.ACCOUNT_CREATION) {
            return;
        }

        logger.info("Received account creation request [onboarding_id={}]", message.onboardingId());

        try {
            var account = createAccount.execute(message.onboardingId(), Currency.BRL);
            logger.info("Account created [onboarding_id={}, account_id={}]",
                    message.onboardingId(), account.accountId());

            OnboardingMessage next = OnboardingMessage.of(
                    message.onboardingId(),
                    OnboardingStep.ACCOUNT_NOTIFICATION,
                    message.data(),
                    message.metadata()
            );
            eventPublisher.publish(next);
        } catch (Exception e) {
            logger.error("Failed to create account [onboarding_id={}]", message.onboardingId(), e);
        }
    }
}
