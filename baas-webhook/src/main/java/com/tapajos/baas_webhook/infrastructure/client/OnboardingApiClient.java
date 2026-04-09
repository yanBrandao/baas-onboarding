package com.tapajos.baas_webhook.infrastructure.client;

import com.tapajos.baas_webhook.domain.UpdateStatusRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class OnboardingApiClient {

    private static final Logger logger = LoggerFactory.getLogger(OnboardingApiClient.class);
    private final RestClient restClient;
    private final String onboardingBaseUrl;

    public OnboardingApiClient(RestClient.Builder restClientBuilder,
                               @Value("${baas.api.onboarding.url}") String onboardingBaseUrl) {
        this.restClient = restClientBuilder.build();
        this.onboardingBaseUrl = onboardingBaseUrl;
    }

    public void updateStatus(String onboardingId, String status) {
        String url = String.format("%s/%s/status", onboardingBaseUrl, onboardingId);
        logger.info("Patching onboarding status at url: {}", url);

        restClient.patch()
                .uri(url)
                .body(new UpdateStatusRequest(status))
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    logger.error("Failed to update status for onboarding {}: {}", onboardingId, response.getStatusCode());
                    throw new RuntimeException("Failed to update onboarding status");
                })
                .toBodilessEntity();
                
        logger.info("Successfully updated onboarding {} status to {}", onboardingId, status);
    }
}
