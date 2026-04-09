# BaaS Onboarding — Project

## Vision
A bank account onboarding case study demonstrating a full microservices workflow:
HTTP intake → DynamoDB persistence → SNS/SQS event pipeline → fraud check → customer + account creation → notification.

## Goals
- Demonstrate clean layered Spring Boot architecture
- Show SNS fan-out / SQS consumer pattern with `baas-common` as shared library
- Provide a minimal but functional React frontend for end-to-end demos

## Stack
| Layer | Technology |
|---|---|
| Backend entry | Spring Boot 3.4.4 (baas-onboarding, port 9001) |
| Fraud service | Spring Boot 4.0.5 (baas-frauds, port 9002) |
| Shared lib | baas-common (Java library, Maven local) |
| Storage | DynamoDB via LocalStack |
| Messaging | SNS + SQS via LocalStack |
| Frontend | React 18 + Vite + TypeScript + Tailwind + shadcn/ui (port 5173) |

## Modules
| Directory | Role |
|---|---|
| `baas-onboarding/` | HTTP intake, DynamoDB, SNS publish |
| `baas-common/` | Shared message types + SNS/SQS publishers |
| `baas-frauds/` | SQS consumer, fraud detection |
| `baas-frontend/` | React onboarding form + status polling |
