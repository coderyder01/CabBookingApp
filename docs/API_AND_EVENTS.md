# APIs and Events

## REST APIs

### `PUT /locations/drivers/{driverId}`

```json
{
  "driverId": "driver-1",
  "tenantId": "ola",
  "region": "in-west",
  "latitude": 19.076,
  "longitude": 72.8777,
  "available": true
}
```

### `GET /locations/drivers/nearby`

Query parameters: `tenantId`, `region`, `latitude`, `longitude`, `limit`.

### `POST /rides`

Headers: `Idempotency-Key`.

```json
{
  "clientRequestId": "ride-1001",
  "tenantId": "ola",
  "region": "in-west",
  "riderId": "rider-9",
  "pickup": {"latitude": 19.0759, "longitude": 72.8776, "label": "BKC"},
  "destination": {"latitude": 19.1197, "longitude": 72.8464, "label": "Andheri"},
  "tier": "GO",
  "paymentMethodId": "pm-card-1"
}
```

### `POST /rides/{rideId}/decline`

```json
{"driverId": "driver-1"}
```

### `POST /trips/{tripId}/start`, `/pause`, `/end`

End trip request:

```json
{"distanceKm": 8.4}
```

### `POST /payments/authorize`

Headers: `Idempotency-Key`.

```json
{
  "tenantId": "ola",
  "riderId": "rider-9",
  "paymentMethodId": "pm-card-1",
  "estimatedAmount": 220.5
}
```

### `POST /notifications`

```json
{
  "tenantId": "ola",
  "recipientId": "driver-1",
  "channel": "PUSH",
  "message": "New ride request"
}
```

### `POST /trips/{tripId}/sos`

```json
{
  "reportedBy": "rider-9",
  "reason": "Unsafe driving"
}
```

## gRPC Internal APIs

The protobuf source of truth lives at `common/src/main/proto/rides.proto`. Dispatch uses these gRPC services for low-latency internal orchestration:

```proto
service LocationGrpcService {
  rpc FindNearbyDrivers(NearbyDriversRequest) returns (NearbyDriversResponse);
}

service SurgeGrpcService {
  rpc RecordDemand(SurgeDemandRequest) returns (SurgeQuoteResponse);
  rpc GetSurgeQuote(SurgeQuoteRequest) returns (SurgeQuoteResponse);
}

service TripGrpcService {
  rpc CreateTrip(CreateTripGrpcRequest) returns (TripGrpcResponse);
}

service PaymentGrpcService {
  rpc AuthorizePayment(PaymentAuthorizationGrpcRequest) returns (PaymentAuthorizationGrpcResponse);
}

service NotificationGrpcService {
  rpc SendNotification(NotificationGrpcRequest) returns (NotificationGrpcResponse);
}
```

## Event Topics

| Topic | Key | Producer | Consumers |
| --- | --- | --- | --- |
| `driver.location.updated.v1` | `tenantId:region:driverId` | Location | Dispatch, Surge, Analytics |
| `ride.requested.v1` | `tenantId:region:clientRequestId` | Dispatch | Surge, Fraud, Analytics |
| `dispatch.offer.created.v1` | `tenantId:region:driverId` | Dispatch | Driver Gateway, Notification |
| `dispatch.offer.declined.v1` | `tenantId:region:driverId` | Dispatch | Dispatch, Analytics |
| `trip.state.changed.v1` | `tenantId:region:tripId` | Trip | Notification, Payment, Support |
| `payment.authorization.created.v1` | `tenantId:region:authorizationId` | Payment | Trip, Reconciliation |
| `safety.incident.opened.v1` | `tenantId:region:incidentId` | Trip | Safety Ops, Notification |

Event envelope:

```json
{
  "eventId": "uuid",
  "topic": "trip.state.changed.v1",
  "tenantId": "ola",
  "region": "in-west",
  "occurredAt": "2026-05-22T10:00:00Z",
  "payload": {}
}
```
