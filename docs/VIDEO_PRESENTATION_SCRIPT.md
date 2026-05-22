# Video Presentation Script

## Slide 1: Title

Hello, this is my design for a multi-region ride-hailing platform similar to Uber or Ola. The system handles real-time driver location ingestion, ride matching, surge pricing, trip lifecycle, payments, notifications, admin controls, and a safety feature.

The key design goal is to meet strict latency targets: dispatch decision within one second at p95, and request-to-acceptance within three seconds at p95.

## Slide 2: Requirements

The platform must support 300k concurrent drivers globally, 60k ride requests per minute at peak, and 500k driver location updates per second globally. It must support multi-tenant and multi-region operation.

The critical architectural constraint is that the hot path should use region-local writes and avoid synchronous cross-region calls.

## Slide 3: HLD

The system is divided into microservices. Dispatch owns ride orchestration. Location owns driver updates and nearby lookup. Surge owns demand and supply calculations. Trip owns trip lifecycle and fares. Payment isolates external PSPs. Notification sends push and SMS messages. Admin owns feature flags and kill switches.

For communication, external clients use REST. Internal service calls from Dispatch use gRPC over HTTP/2 with protobuf. Eureka provides service discovery locally. Kafka or Pulsar is used for async domain events. Redis is used for hot location and surge data, and Postgres or CockroachDB is used for transactional records.

## Slide 4: Ride Flow

The rider sends a ride request to Dispatch. Dispatch computes the pickup geo-cell and records demand in Surge. It calls Location to find nearby available drivers. It ranks the candidates, gets a surge quote, authorizes payment, creates the trip, and sends notifications.

This path is designed to complete within the one-second dispatch decision budget.

## Slide 5: Dispatch LLD

Dispatch is the chosen LLD component because it is the most latency-sensitive. It uses idempotency keys to protect against duplicate mobile requests. It uses a nearest-driver ranking strategy for the current implementation, and it supports reassignment when a driver declines.

In production, the ranking model can be extended with ETA, acceptance score, tier eligibility, fraud signals, safety filters, and balancing logic.

## Slide 6: gRPC Decision

REST is kept for public APIs because it is mobile-friendly and easy to debug. gRPC is used internally because it gives typed contracts, compact protobuf payloads, HTTP/2 multiplexing, and lower overhead than JSON REST.

This split gives us developer ergonomics at the edge and performance inside the platform.

## Slide 7: Data Model

The main entities for Dispatch are Ride Request, Dispatch Offer, Driver Location Snapshot, Surge Quote, Payment Authorization, and Trip. Ride Request is linked to offers, payment authorization, surge quote, and final trip.

The ERD is focused on Dispatch/Matching because it is the selected deep-dive component.

## Slide 8: Resilience

The system uses idempotency for every mobile mutation. Internal calls use short deadlines and circuit breakers. Surge can degrade to a default multiplier of 1.0. Notification failures do not block the ride decision. Payment is isolated behind Payment Service because PSP latency is outside our control.

Backpressure is handled by sampling location updates, shedding low-priority retries, and using Kafka/Pulsar retry and dead-letter topics.

## Slide 9: Multi-Region

Each region has local Dispatch, Location, Surge, Trip, Payment, Redis, Kafka, and database capacity. The hot path never waits for cross-region replication. Cross-region sync is asynchronous and used for analytics, reporting, reconciliation, and disaster recovery.

## Slide 10: Feature of Choice

I added a Trip SOS and safety incident feature. A rider or driver can call `POST /trips/{tripId}/sos` to open an incident. In production, this would notify safety operations, attach recent locations, create a support case, and optionally escalate to emergency workflows.

## Slide 11: Demo

For the demo, I start Eureka first, then the microservices. I seed driver locations through Location Service. Then I create a ride through Dispatch. Dispatch uses gRPC internally to find drivers, calculate surge, authorize payment, create the trip, and send notifications.

I can also simulate driver decline and trigger reassignment, then trigger the SOS endpoint.

## Slide 12: Closing

The final design balances latency, scale, and operational safety. REST is used at the edge, gRPC on the internal hot path, Redis for real-time data, Kafka/Pulsar for events, Postgres or CockroachDB for transactions, and region-local writes for latency.
