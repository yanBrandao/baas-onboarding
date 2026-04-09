package com.tapajos.baas.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "baas.sqs")
public class OnboardingSqsProperties {

    /** SQS queue name for fraud detection. */
    private String queueName = "baas-fraud-queue";

    /** AWS endpoint override — point to LocalStack in local environments. */
    private String endpoint = "http://localhost:4566";

    /** AWS region. */
    private String region = "us-east-1";

    /** Managed thread pool size for SQS listeners. */
    private int concurrency = 5;

    public String getQueueName() { return queueName; }
    public void setQueueName(String queueName) { this.queueName = queueName; }

    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public int getConcurrency() { return concurrency; }
    public void setConcurrency(int concurrency) { this.concurrency = concurrency; }
}
