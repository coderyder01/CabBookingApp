# Multi-Region Ride-Hailing Platform

End-to-end assignment solution for the provided SSE architecture problem statement. The repository contains a Maven multi-module Java 17 / Spring Boot implementation plus architecture deliverables.

## Services

| Service | Port | Responsibility |
| --- | ---: | --- |
| discovery-service | 8761 | Spring Cloud Netflix Eureka service registry |
| dispatch-service | REST 8080 | Ride request orchestration, matching, idempotency, reassignment |
| location-service | REST 8081, gRPC 9091 | Real-time driver location ingestion and nearby driver lookup |
| surge-service | REST 8082, gRPC 9092 | Geo-cell supply/demand stats and surge multiplier |
| trip-service | REST 8083, gRPC 9093 | Trip lifecycle, fare calculation, receipts, SOS incidents |
| payment-service | REST 8084, gRPC 9094 | PSP authorization facade with idempotency |
| notification-service | REST 8085, gRPC 9095 | Push/SMS queue facade |
| admin-service | 8086 | Feature flags and kill switches |

## Build

```bash
mvn -DskipTests package
```

## Run Locally

Start each service in a separate terminal:

```bash
mvn -pl services/discovery-service spring-boot:run
mvn -pl services/location-service spring-boot:run
mvn -pl services/surge-service spring-boot:run
mvn -pl services/trip-service spring-boot:run
mvn -pl services/payment-service spring-boot:run
mvn -pl services/notification-service spring-boot:run
mvn -pl services/admin-service spring-boot:run
mvn -pl services/dispatch-service spring-boot:run
```

Eureka dashboard: http://localhost:8761. Public/mobile APIs remain REST. Dispatch uses gRPC stubs over HTTP/2 for internal calls to Location, Surge, Trip, Payment, and Notification services. The protobuf contract is in `common/src/main/proto/rides.proto`.

Seed driver locations:

```bash
curl -X PUT http://localhost:8081/locations/drivers/driver-1 \
  -H 'Content-Type: application/json' \
  -d '{"driverId":"driver-1","tenantId":"ola","region":"in-west","latitude":19.076,"longitude":72.8777,"available":true}'

curl -X PUT http://localhost:8081/locations/drivers/driver-2 \
  -H 'Content-Type: application/json' \
  -d '{"driverId":"driver-2","tenantId":"ola","region":"in-west","latitude":19.081,"longitude":72.880,"available":true}'
```

Request a ride:

```bash
curl -X POST http://localhost:8080/rides \
  -H 'Content-Type: application/json' \
  -H 'Idempotency-Key: req-1001' \
  -d '{
    "clientRequestId": "ride-1001",
    "tenantId": "ola",
    "region": "in-west",
    "riderId": "rider-9",
    "pickup": {"latitude": 19.0759, "longitude": 72.8776, "label": "BKC"},
    "destination": {"latitude": 19.1197, "longitude": 72.8464, "label": "Andheri"},
    "tier": "GO",
    "paymentMethodId": "pm-card-1"
  }'
```

Driver decline and reassignment:

```bash
curl -X POST http://localhost:8080/rides/ride-1001/decline \
  -H 'Content-Type: application/json' \
  -d '{"driverId":"driver-1"}'
```

Safety feature:

```bash
curl -X POST http://localhost:8083/trips/{tripId}/sos \
  -H 'Content-Type: application/json' \
  -d '{"reportedBy":"rider-9","reason":"Unsafe driving"}'
```

## Deliverables

- [Architecture](docs/ARCHITECTURE.md)
- [API and Event Contracts](docs/API_AND_EVENTS.md)
- [Resilience Plan](docs/RESILIENCE.md)
- [Video Script](docs/VIDEO_SCRIPT.md)
