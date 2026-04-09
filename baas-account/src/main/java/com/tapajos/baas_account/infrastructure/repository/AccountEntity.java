package com.tapajos.baas_account.infrastructure.repository;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.extensions.annotations.DynamoDbVersionAttribute;

import java.math.BigDecimal;

@DynamoDbBean
public class AccountEntity {

    private String accountId;
    private String onboardingId;
    private String currency;
    private BigDecimal balance;
    private String status;
    private Long version;
    private String createdAt;

    public AccountEntity() {}

    @DynamoDbPartitionKey
    @DynamoDbAttribute("account_id")
    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }

    @DynamoDbAttribute("onboarding_id")
    public String getOnboardingId() { return onboardingId; }
    public void setOnboardingId(String onboardingId) { this.onboardingId = onboardingId; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @DynamoDbVersionAttribute
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    @DynamoDbAttribute("created_at")
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
