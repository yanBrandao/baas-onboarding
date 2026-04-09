# Feature: Kafka Topic-Per-Service

## Problem
All four services consume from a single `baas-onboarding` topic and filter by `step`.
Every consumer sees every message — wasteful, brittle, and unscalable.

## Goal
Each service owns a dedicated Kafka topic. Producers write exclusively to the next service's topic.

## Topology

```
baas-onboarding  →  [baas-frauds]   →  baas-frauds
baas-frauds      →  [baas-account]  →  baas-account
baas-account     →  [baas-webhook]  →  baas-webhook
baas-webhook     →  (terminal — no publish)
```

## Requirements

| ID   | Requirement |
|------|-------------|
| R001 | `baas-onboarding` publishes onboarding events to topic `baas-frauds` |
| R002 | `baas-frauds` consumes from topic `baas-frauds`; publishes next step to `baas-account` |
| R003 | `baas-account` consumes from topic `baas-account`; publishes next step to `baas-webhook` |
| R004 | `baas-webhook` consumes from topic `baas-webhook`; does not publish further |
| R005 | Each service's consumer topic is configured via `baas.kafka.consumer-topic` in application.yaml |
| R006 | Each service's producer topic (`baas.kafka.topic`) points to the next service's topic |
| R007 | `@KafkaListener` annotations use `${baas.kafka.consumer-topic}` (not the shared publish topic) |

## Out of Scope
- Removing step-based filtering inside listeners (kept as defensive guard)
- Changing message schema or `OnboardingStep` enum
- Changes to `baas-common` internals
