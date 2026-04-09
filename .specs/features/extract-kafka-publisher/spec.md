# Feature: Extract Kafka Publisher from baas-common

## Problem
`OnboardingKafkaPublisher`, `OnboardingKafkaProperties`, and `BaasCommonAutoConfiguration` live in
`baas-common` but are Kafka infrastructure concerns — not shared contracts. This forces all
consumers of `baas-common` to inherit Kafka producer setup they may not need or want to customize.

## Goal
Make `baas-common` a pure message-contract library. Each service that publishes owns its own
Kafka publisher implementation and wiring.

## What stays in baas-common
- `OnboardingEventPublisher` interface — the shared publishing contract
- All message types (`OnboardingMessage`, `OnboardingStep`, `OnboardingMetadata`, etc.)

## What moves out of baas-common
- `OnboardingKafkaPublisher` — removed; each publishing service gets a local `KafkaEventPublisher`
- `OnboardingKafkaProperties` — removed; each service reads its own `spring.kafka.bootstrap-servers`
- `BaasCommonAutoConfiguration` — removed entirely
- `spring-kafka` and `spring-boot-autoconfigure` — removed from baas-common dependencies

## Publishing services
| Service | Publishes to |
|---|---|
| baas-onboarding | baas-frauds topic |
| baas-frauds | baas-account topic |
| baas-account | baas-webhook topic |
| baas-webhook | (terminal — no publish) |

## Requirements
| ID   | Requirement |
|------|-------------|
| R001 | baas-common contains only the interface and message types — no Kafka infrastructure |
| R002 | Each publishing service has `infrastructure/kafka/KafkaEventPublisher.java` implementing `OnboardingEventPublisher` |
| R003 | Each publishing service has `infrastructure/config/KafkaPublisherConfig.java` wiring the publisher bean |
| R004 | baas-onboarding uses `spring.kafka.bootstrap-servers` (standard property); `baas.kafka.bootstrap-servers` removed |
| R005 | `KafkaEventPublisher` in baas-onboarding has unit tests covering happy path and serialization |
| R006 | All existing tests continue to pass after the refactoring |
