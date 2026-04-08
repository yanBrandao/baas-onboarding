package com.tapajos.baas.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "baas.sns")
public class OnboardingSnsProperties {

    /** SNS topic ARN to publish onboarding messages to. */
    private String topicArn = "arn:aws:sns:us-east-1:000000000000:baas-onboarding";

    /** AWS endpoint override — point to LocalStack in local environments. */
    private String endpoint = "http://localhost:4566";

    /** AWS region. */
    private String region = "us-east-1";

    public String getTopicArn() { return topicArn; }
    public void setTopicArn(String topicArn) { this.topicArn = topicArn; }

    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
}
