package com.tapajos.baas_onboarding.infrastructure.repository;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@DynamoDbBean
public class OnboardingEntity {

    private String onboardingId;
    private String status;
    private String name;
    private String email;
    private String phone;
    private String document;
    private String birthDate;
    private String motherName;
    private String fingerprint;
    private AddressEntity address;

    public OnboardingEntity() {}

    @DynamoDbPartitionKey
    @DynamoDbAttribute("onboarding_id")
    public String getOnboardingId() { return onboardingId; }
    public void setOnboardingId(String onboardingId) { this.onboardingId = onboardingId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getDocument() { return document; }
    public void setDocument(String document) { this.document = document; }

    @DynamoDbAttribute("birth_date")
    public String getBirthDate() { return birthDate; }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }

    @DynamoDbAttribute("mother_name")
    public String getMotherName() { return motherName; }
    public void setMotherName(String motherName) { this.motherName = motherName; }

    public String getFingerprint() { return fingerprint; }
    public void setFingerprint(String fingerprint) { this.fingerprint = fingerprint; }

    public AddressEntity getAddress() { return address; }
    public void setAddress(AddressEntity address) { this.address = address; }
}
