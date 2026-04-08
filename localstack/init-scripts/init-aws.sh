#!/bin/bash
echo "Initializing Localstack resources..."

# Create SNS Topic
awslocal --region us-east-1 --endpoint-url=http://localhost:4566 sns create-topic --name baas-onboarding
echo "SNS topic 'baas-onboarding' created."

# Create DynamoDB Table
awslocal --region us-east-1 --endpoint-url=http://localhost:4566 dynamodb create-table \
    --table-name Onboarding \
    --attribute-definitions AttributeName=onboardingId,AttributeType=S \
    --key-schema AttributeName=onboardingId,KeyType=HASH \
    --provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5
echo "DynamoDB table 'Onboarding' created."

echo "Localstack initialization complete."
