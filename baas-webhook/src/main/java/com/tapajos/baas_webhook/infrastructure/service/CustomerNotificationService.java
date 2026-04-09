package com.tapajos.baas_webhook.infrastructure.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CustomerNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(CustomerNotificationService.class);

    public void sendEmailNotification(String email, String onboardingId, String status) {
        String subject = "COMPLETED".equals(status)
                ? "Your account has been successfully created!"
                : "Action Required: Account creation failed";

        String body = "COMPLETED".equals(status)
                ? "Congratulations! Your onboarding (" + onboardingId + ") is complete."
                : "We couldn't process your onboarding (" + onboardingId + "). Please review your documents.";

        // In production this would send an actual email (e.g. via SES, SendGrid, etc.)
        logger.info("[EMAIL NOTIFICATION] to={} subject=\"{}\" body=\"{}\"", email, subject, body);
    }
}
