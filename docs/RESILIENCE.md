# Resilience Plan

## Idempotency

All mobile-facing mutation APIs accept an idempotency key. Dispatch uses rider/client request IDs when no explicit key is provided. Payment authorization always forwards a deterministic key to avoid double charging.

## Retries and Timeouts

Clients retry safely with the same idempotency key. Internal calls use short timeouts on the dispatch hot path. PSP calls are isolated behind Payment Service and should use bounded retries with exponential backoff, jitter, and reconciliation jobs.

## Backpressure

Location ingestion should reject or sample non-critical updates when Redis/Kafka lag rises. Dispatch should shed low-priority retries before fresh ride requests. Kafka/Pulsar partitions are keyed by `tenantId + region + geoCell` to avoid a single global bottleneck.

## Circuit Breakers

Dispatch should fail fast if Payment, Trip, or Location are unhealthy. Surge can degrade to multiplier `1.0`; Notifications can queue asynchronously and should not block trip creation.

## Failure Modes

| Failure | Behavior |
| --- | --- |
| Driver app flaky | Offer expires; Dispatch reassigns |
| PSP slow/down | Payment Service returns pending/failure; retries/reconciliation continue off hot path |
| Surge unavailable | Use default multiplier and emit degradation metric |
| Location cache partial outage | Restrict matching to healthy geo shards |
| Region outage | Route new requests to failover region; hot path never depends on synchronous cross-region writes |
| Duplicate mobile requests | Idempotency key returns same decision |

## Compliance

Payment Service stores PSP tokens only, never raw card data. PII fields should be encrypted at rest, audit logged, and deletable/anonymizable for GDPR/DPDP. Event payloads should avoid unnecessary PII.
