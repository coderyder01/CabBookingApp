# 5-10 Minute Video Script

1. Introduce the problem: multi-tenant, multi-region ride-hailing with strict dispatch latency and high location update volume.
2. Show the HLD diagram in `docs/ARCHITECTURE.md`: mobile clients, Eureka discovery, services, Redis, Kafka/Pulsar, Postgres/CockroachDB, external PSPs.
3. Explain the hot path: rider request enters Dispatch, Dispatch queries nearby drivers from Location, asks Surge for multiplier, authorizes Payment, creates Trip, and sends Notifications.
4. Explain why region-local writes matter: no cross-region sync in the dispatch decision path, asynchronous replication for analytics and recovery.
5. Deep dive into Dispatch/Matching: idempotency, driver ranking, decline/reassign, downstream timeouts.
6. Show API contracts in `docs/API_AND_EVENTS.md` and event topic partitioning.
7. Show resilience plan: retries, backpressure, circuit breakers, PSP isolation, duplicate request safety.
8. Demo locally: seed driver location, request ride, show assigned trip, simulate driver decline, trigger SOS endpoint.
9. Close with trade-offs: in-memory demo implementation for review, production adapters map to Redis, Kafka/Pulsar, and Postgres/CockroachDB.
