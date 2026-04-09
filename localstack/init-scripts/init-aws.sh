#!/bin/bash
echo "Initializing Localstack resources..."

# Create DynamoDB Table for onboarding
awslocal --region us-east-1 --endpoint-url=http://localhost:4566 dynamodb create-table \
    --table-name Onboarding \
    --attribute-definitions AttributeName=onboarding_id,AttributeType=S \
    --key-schema AttributeName=onboarding_id,KeyType=HASH \
    --provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5
echo "DynamoDB table 'Onboarding' created."

# Create Account DynamoDB Table
awslocal --region us-east-1 --endpoint-url=http://localhost:4566 dynamodb create-table \
    --table-name Account \
    --attribute-definitions AttributeName=account_id,AttributeType=S \
    --key-schema AttributeName=account_id,KeyType=HASH \
    --provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5
echo "DynamoDB table 'Account' created."

# Create Transaction DynamoDB Table (account_id PK + transaction_id SK)
awslocal --region us-east-1 --endpoint-url=http://localhost:4566 dynamodb create-table \
    --table-name Transaction \
    --attribute-definitions \
        AttributeName=account_id,AttributeType=S \
        AttributeName=transaction_id,AttributeType=S \
    --key-schema \
        AttributeName=account_id,KeyType=HASH \
        AttributeName=transaction_id,KeyType=RANGE \
    --provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5
echo "DynamoDB table 'Transaction' created."

echo "Localstack initialization complete."
