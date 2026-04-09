package com.tapajos.baas_webhook.controller;

import com.tapajos.baas_webhook.domain.AccountEventPayload;
import com.tapajos.baas_webhook.usecase.ProcessAccountEventUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/webhook")
public class WebhookController {

    private static final Logger logger = LoggerFactory.getLogger(WebhookController.class);
    private final ProcessAccountEventUseCase processAccountEventUseCase;

    public WebhookController(ProcessAccountEventUseCase processAccountEventUseCase) {
        this.processAccountEventUseCase = processAccountEventUseCase;
    }

    @PostMapping("/account-events")
    public ResponseEntity<String> handleAccountEvent(@RequestBody AccountEventPayload payload) {
        logger.info("Received account event for onboarding: {}", payload.onboardingId());
        
        try {
            processAccountEventUseCase.execute(payload);
            return ResponseEntity.ok("Event processed successfully");
        } catch (Exception e) {
            logger.error("Failed to process account event", e);
            // We return 500 so the caller knows the webhook failed to process completely
            return ResponseEntity.internalServerError().body("Failed to process event");
        }
    }
}
