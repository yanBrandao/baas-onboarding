#!/bin/bash
echo "Initializing Localstack resources..."

# Create SNS Topic
awslocal --region us-east-1 --endpoint-url=http://localhost:4566 sns create-topic --name baas-onboarding
echo "SNS topic 'baas-onboarding' created."

# Create DynamoDB Table
awslocal --region us-east-1 --endpoint-url=http://localhost:4566 dynamodb create-table \
    --table-name Onboarding \
    --attribute-definitions AttributeName=onboarding_id,AttributeType=S \
    --key-schema AttributeName=onboarding_id,KeyType=HASH \
    --provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5
echo "DynamoDB table 'Onboarding' created."

# Create SQS Queue
awslocal --region us-east-1 --endpoint-url=http://localhost:4566 sqs create-queue --queue-name baas-fraud-queue
echo "SQS queue 'baas-fraud-queue' created."

# Subscribe SQS to SNS with RawMessageDelivery so the raw JSON payload reaches the listener
# without the SNS notification envelope wrapper
awslocal --region us-east-1 --endpoint-url=http://localhost:4566 sns subscribe \
    --topic-arn arn:aws:sns:us-east-1:000000000000:baas-onboarding \
    --protocol sqs \
    --notification-endpoint arn:aws:sqs:us-east-1:000000000000:baas-fraud-queue \
    --attributes RawMessageDelivery=true
echo "SQS queue 'baas-fraud-queue' subscribed to SNS topic 'baas-onboarding'."

echo "Localstack initialization complete."
