import pptxgen from "/Users/sushilpandey/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/pptxgenjs/dist/pptxgen.es.js";

const pptx = new pptxgen();
pptx.layout = "LAYOUT_WIDE";
pptx.author = "Codex";
pptx.subject = "Multi-region ride-hailing platform architecture";
pptx.title = "Multi-Region Ride-Hailing Platform";
pptx.company = "Assignment Deliverables";
pptx.lang = "en-US";
pptx.theme = {
  headFontFace: "Aptos Display",
  bodyFontFace: "Aptos",
  lang: "en-US"
};
pptx.defineLayout({ name: "CUSTOM_WIDE", width: 13.333, height: 7.5 });
pptx.layout = "CUSTOM_WIDE";

const C = {
  ink: "10202A",
  muted: "56636B",
  faint: "EFF3F6",
  line: "C8D2DA",
  teal: "0F766E",
  blue: "2563EB",
  green: "16A34A",
  amber: "D97706",
  red: "DC2626",
  white: "FFFFFF"
};

function slideTitle(slide, kicker, title) {
  slide.addText(kicker.toUpperCase(), { x: 0.55, y: 0.32, w: 3.6, h: 0.2, fontSize: 8, bold: true, color: C.teal, charSpace: 1.2, margin: 0 });
  slide.addText(title, { x: 0.55, y: 0.58, w: 11.8, h: 0.45, fontSize: 24, bold: true, color: C.ink, margin: 0 });
  slide.addShape(pptx.ShapeType.line, { x: 0.55, y: 1.15, w: 12.2, h: 0, line: { color: C.line, width: 1 } });
}

function footer(slide, n) {
  slide.addText(`Multi-Region Ride-Hailing Platform | ${n}`, { x: 0.55, y: 7.08, w: 5, h: 0.16, fontSize: 7, color: C.muted, margin: 0 });
}

function box(slide, text, x, y, w, h, fill = C.white, line = C.line, color = C.ink) {
  slide.addShape(pptx.ShapeType.roundRect, { x, y, w, h, rectRadius: 0.04, fill: { color: fill }, line: { color: line, width: 1 } });
  slide.addText(text, { x: x + 0.1, y: y + 0.08, w: w - 0.2, h: h - 0.12, fontSize: 10, bold: true, color, valign: "mid", align: "center", margin: 0.02, fit: "shrink" });
}

function arrow(slide, x1, y1, x2, y2, color = C.muted) {
  slide.addShape(pptx.ShapeType.line, { x: x1, y: y1, w: x2 - x1, h: y2 - y1, line: { color, width: 1.2, beginArrowType: "none", endArrowType: "triangle" } });
}

function bullets(slide, items, x, y, w, h, fontSize = 13) {
  slide.addText(items.map(t => ({ text: t, options: { bullet: { type: "ul" } } })), {
    x, y, w, h, fontSize, color: C.ink, breakLine: false, fit: "shrink",
    paraSpaceAfterPt: 8, margin: 0.04
  });
}

function addNotes(slide, lines) {
  slide.addNotes(lines.join("\n"));
}

let s = pptx.addSlide();
s.background = { color: "F8FAFC" };
s.addText("Multi-Region Ride-Hailing Platform", { x: 0.65, y: 0.72, w: 8.8, h: 0.65, fontSize: 32, bold: true, color: C.ink, margin: 0 });
s.addText("Java 17 | Spring Boot | Maven | Eureka | gRPC | Kafka/Pulsar | Redis | Postgres/CockroachDB", { x: 0.68, y: 1.48, w: 9.8, h: 0.3, fontSize: 13, color: C.muted, margin: 0 });
box(s, "REST edge", 0.75, 2.35, 2.05, 0.65, C.faint, C.line, C.ink);
box(s, "gRPC hot path", 3.15, 2.35, 2.25, 0.65, "E8F5F3", C.teal, C.teal);
box(s, "Redis + Kafka", 5.75, 2.35, 2.25, 0.65, "EFF6FF", C.blue, C.blue);
box(s, "Regional writes", 8.35, 2.35, 2.45, 0.65, "FFF7ED", C.amber, C.amber);
s.addShape(pptx.ShapeType.rect, { x: 0.75, y: 4.05, w: 11.85, h: 1.65, fill: { color: C.ink }, line: { color: C.ink } });
s.addText("Goal: dispatch decision p95 <= 1s, request-to-acceptance p95 <= 3s", { x: 1.05, y: 4.5, w: 11.2, h: 0.5, fontSize: 20, bold: true, color: C.white, margin: 0 });
footer(s, 1);
addNotes(s, ["Introduce the assignment and the main architectural choices: REST at the edge, gRPC internally, Redis/Kafka for hot and async paths, and region-local writes."]);

s = pptx.addSlide();
slideTitle(s, "Requirements", "What the platform must handle");
const reqRows = [
  ["Dispatch", "p95 <= 1s decision, p95 <= 3s request-to-acceptance"],
  ["Scale", "300k concurrent drivers, 60k rides/min peak, 500k location updates/sec globally"],
  ["Regions", "Region-local writes, no cross-region sync on hot path"],
  ["Reliability", "99.95% dispatch availability, idempotent mobile APIs"],
  ["Compliance", "PCI, PII encryption, GDPR/DPDP"]
];
s.addTable(reqRows, { x: 0.75, y: 1.55, w: 11.8, h: 4.6, border: { color: C.line, pt: 1 }, fill: { color: C.white }, color: C.ink, fontSize: 13, margin: 0.08, autoFit: false, colW: [2.1, 9.7] });
footer(s, 2);
addNotes(s, ["Summarize functional and non-functional requirements. Stress that latency and multi-region constraints drive the technical decisions."]);

s = pptx.addSlide();
slideTitle(s, "HLD", "Bounded contexts and communication model");
box(s, "Rider App\nREST", 0.65, 1.55, 1.45, 0.7, "F8FAFC");
box(s, "Driver App\nREST", 0.65, 3.3, 1.45, 0.7, "F8FAFC");
box(s, "Dispatch\n8080", 2.75, 2.18, 1.55, 0.8, "E8F5F3", C.teal, C.teal);
box(s, "Location\n8081/9091", 5.05, 1.25, 1.65, 0.65);
box(s, "Surge\n8082/9092", 5.05, 2.05, 1.65, 0.65);
box(s, "Payment\n8084/9094", 5.05, 2.85, 1.65, 0.65);
box(s, "Trip\n8083/9093", 5.05, 3.65, 1.65, 0.65);
box(s, "Notify\n8085/9095", 5.05, 4.45, 1.65, 0.65);
box(s, "Eureka\n8761", 2.85, 4.75, 1.35, 0.6, "EFF6FF", C.blue, C.blue);
box(s, "Redis\nHot KV/GEO", 8.1, 1.45, 1.65, 0.68, "F8FAFC");
box(s, "Kafka/Pulsar\nEvents", 8.1, 2.55, 1.65, 0.68, "F8FAFC");
box(s, "Postgres/\nCockroachDB", 8.1, 3.65, 1.65, 0.68, "F8FAFC");
box(s, "External PSP", 10.7, 2.85, 1.45, 0.68, "FFF7ED", C.amber, C.amber);
arrow(s, 2.1, 1.9, 2.75, 2.38);
arrow(s, 2.1, 3.65, 2.75, 2.78);
arrow(s, 4.3, 2.45, 5.05, 1.58, C.teal);
arrow(s, 4.3, 2.45, 5.05, 2.38, C.teal);
arrow(s, 4.3, 2.45, 5.05, 3.18, C.teal);
arrow(s, 4.3, 2.45, 5.05, 3.98, C.teal);
arrow(s, 4.3, 2.45, 5.05, 4.78, C.teal);
arrow(s, 6.7, 1.58, 8.1, 1.78);
arrow(s, 6.7, 2.38, 8.1, 2.9);
arrow(s, 6.7, 3.98, 8.1, 3.98);
arrow(s, 6.7, 3.18, 10.7, 3.18, C.amber);
s.addText("Public edge = REST/JSON\nInternal hot path = gRPC/HTTP2 + protobuf", { x: 0.75, y: 5.82, w: 11.4, h: 0.45, fontSize: 14, bold: true, color: C.ink, align: "center", margin: 0 });
footer(s, 3);
addNotes(s, ["Walk through each service and clarify why REST is used at the edge and gRPC internally."]);

s = pptx.addSlide();
slideTitle(s, "Ride Flow", "Request-to-assignment hot path");
const flow = ["POST /rides", "Record demand", "Nearby drivers", "Surge quote", "Authorize", "Create trip", "Notify", "ASSIGNED"];
flow.forEach((t, i) => {
  const x = 0.62 + i * 1.55;
  box(s, t, x, 2.35, 1.25, 0.72, i === 0 || i === 7 ? "E8F5F3" : C.white, i === 0 || i === 7 ? C.teal : C.line, i === 0 || i === 7 ? C.teal : C.ink);
  if (i < flow.length - 1) arrow(s, x + 1.25, 2.71, x + 1.55, 2.71, C.teal);
});
bullets(s, [
  "Idempotency protects flaky mobile clients from duplicate ride creation.",
  "Driver ranking starts simple: tenant + region + availability + nearest distance.",
  "Notifications are modeled synchronously in demo, async/queued in production."
], 1.0, 4.15, 11.2, 1.55, 14);
footer(s, 4);
addNotes(s, ["Explain the exact data flow from rider request to assignment response."]);

s = pptx.addSlide();
slideTitle(s, "LLD", "Dispatch / Matching deep dive");
box(s, "Input\nRideRequest", 0.85, 1.45, 1.5, 0.65, "F8FAFC");
box(s, "Idempotency\ncheck", 2.85, 1.45, 1.5, 0.65);
box(s, "Geo-cell\ncompute", 4.85, 1.45, 1.5, 0.65);
box(s, "Candidate\nfetch", 6.85, 1.45, 1.5, 0.65);
box(s, "Rank + filter\nnearest", 8.85, 1.45, 1.5, 0.65);
box(s, "Trip + payment\ncommit", 10.85, 1.45, 1.5, 0.65, "E8F5F3", C.teal, C.teal);
for (let i = 0; i < 5; i++) arrow(s, 2.35 + i * 2, 1.78, 2.85 + i * 2, 1.78, C.teal);
bullets(s, [
  "Current ranking: available drivers in same tenant/region sorted by haversine distance.",
  "Decline path: exclude previous driver and run matching again.",
  "Production ranking can add ETA, traffic, tier eligibility, acceptance score, safety score, and fairness."
], 0.95, 3.0, 5.6, 2.2, 13);
bullets(s, [
  "Latency budget: location 150 ms, surge 80 ms, payment 300 ms, trip 150 ms, buffer 300 ms.",
  "No cross-region sync on the hot path.",
  "gRPC deadlines and circuit breakers keep Dispatch from hanging on dependencies."
], 6.9, 3.0, 5.6, 2.2, 13);
footer(s, 5);
addNotes(s, ["Deep dive into Dispatch and Matching. Mention why this was selected for LLD."]);

s = pptx.addSlide();
slideTitle(s, "APIs", "REST outside, gRPC inside");
box(s, "REST\nPOST /rides\nPUT /locations\nPOST /trips/{id}/sos", 0.9, 1.55, 3.25, 1.7, "F8FAFC");
box(s, "gRPC\nFindNearbyDrivers\nGetSurgeQuote\nAuthorizePayment\nCreateTrip\nSendNotification", 5.0, 1.3, 3.25, 2.2, "E8F5F3", C.teal, C.teal);
box(s, "Events\nride.requested.v1\ntrip.state.changed.v1\npayment.authorization.created.v1\nsafety.incident.opened.v1", 9.05, 1.55, 3.25, 1.7, "EFF6FF", C.blue, C.blue);
bullets(s, [
  "Protobuf source: common/src/main/proto/rides.proto",
  "Event envelope includes eventId, topic, tenantId, region, occurredAt, payload",
  "Kafka/Pulsar partitions by tenant + region + entity/geo-cell"
], 1.15, 4.25, 10.9, 1.2, 14);
footer(s, 6);
addNotes(s, ["Show the API split and mention the main event topics."]);

s = pptx.addSlide();
slideTitle(s, "Data Model", "Dispatch / Matching ERD");
const entities = [
  ["RIDE_REQUEST", 0.8, 1.45],
  ["DISPATCH_OFFER", 3.4, 1.45],
  ["DRIVER_LOCATION_SNAPSHOT", 6.0, 1.45],
  ["SURGE_QUOTE", 0.8, 3.75],
  ["PAYMENT_AUTHORIZATION", 3.4, 3.75],
  ["TRIP", 6.0, 3.75],
  ["SAFETY_INCIDENT", 8.6, 3.75]
];
entities.forEach(([name, x, y]) => box(s, name, x, y, 2.05, 0.72, name === "RIDE_REQUEST" ? "E8F5F3" : C.white, name === "RIDE_REQUEST" ? C.teal : C.line, name === "RIDE_REQUEST" ? C.teal : C.ink));
arrow(s, 2.85, 1.81, 3.4, 1.81);
arrow(s, 5.45, 1.81, 6.0, 1.81);
arrow(s, 1.82, 2.17, 1.82, 3.75);
arrow(s, 2.05, 2.17, 3.95, 3.75);
arrow(s, 2.28, 2.17, 6.6, 3.75);
arrow(s, 8.05, 4.11, 8.6, 4.11, C.red);
bullets(s, [
  "Ride Request links the orchestration record to offers, quote, authorization, and trip.",
  "Driver location snapshots are hot data in Redis; durable event stream preserves history.",
  "Safety incident is the chosen additional feature."
], 0.95, 5.45, 11.2, 0.8, 13);
footer(s, 7);
addNotes(s, ["Explain the ERD and how hot state differs from durable transactional state."]);

s = pptx.addSlide();
slideTitle(s, "Resilience", "Retries, backpressure, circuit breakers");
const resRows = [
  ["Duplicate mobile request", "Idempotency key returns same decision"],
  ["Surge unavailable", "Default multiplier 1.0 + metric"],
  ["Notification failure", "Queue/retry; do not fail trip creation"],
  ["PSP slow", "Timeout, retry, pending state, reconciliation"],
  ["Location pressure", "Sample stale updates; shed low-priority traffic"],
  ["Region outage", "Failover new requests; async replication catches up"]
];
s.addTable(resRows, { x: 0.75, y: 1.5, w: 11.8, h: 4.6, border: { color: C.line, pt: 1 }, fill: { color: C.white }, color: C.ink, fontSize: 12.5, margin: 0.08, colW: [3.5, 8.3] });
footer(s, 8);
addNotes(s, ["Cover resilience across retries, backpressure, circuit breakers, and failure modes."]);

s = pptx.addSlide();
slideTitle(s, "Multi-Region", "Region-local writes on the hot path");
box(s, "Region A\nDispatch + Redis + DB + Kafka", 0.9, 1.55, 3.1, 1.25, "E8F5F3", C.teal, C.teal);
box(s, "Region B\nDispatch + Redis + DB + Kafka", 5.05, 1.55, 3.1, 1.25, "EFF6FF", C.blue, C.blue);
box(s, "Analytics / DR\nAsync replication", 9.2, 1.55, 3.1, 1.25, "FFF7ED", C.amber, C.amber);
arrow(s, 4.0, 2.17, 5.05, 2.17, C.line);
arrow(s, 8.15, 2.17, 9.2, 2.17, C.line);
bullets(s, [
  "Hot path never waits for synchronous cross-region writes.",
  "Region-local Redis and database writes preserve dispatch latency.",
  "Asynchronous replication supports analytics, reconciliation, and disaster recovery."
], 1.05, 4.0, 11.0, 1.45, 15);
footer(s, 9);
addNotes(s, ["Explain region-local write strategy and failover trade-offs."]);

s = pptx.addSlide();
slideTitle(s, "Feature", "Trip SOS / safety incident");
box(s, "POST /trips/{tripId}/sos", 1.0, 1.55, 3.4, 0.8, "FEE2E2", C.red, C.red);
box(s, "Safety incident\nstate = OPEN", 5.0, 1.55, 2.6, 0.8, "FFF7ED", C.amber, C.amber);
box(s, "Ops alert +\nlocation context", 8.25, 1.55, 2.8, 0.8, "E8F5F3", C.teal, C.teal);
arrow(s, 4.4, 1.95, 5.0, 1.95, C.red);
arrow(s, 7.6, 1.95, 8.25, 1.95, C.red);
bullets(s, [
  "Implemented in Trip Service as an additional assignment feature.",
  "Production workflow publishes safety.incident.opened.v1, alerts support, and attaches recent trip context.",
  "This is domain-critical for rider and driver trust."
], 1.1, 3.45, 10.7, 1.55, 15);
footer(s, 10);
addNotes(s, ["Explain why SOS is a valuable feature and how it is implemented."]);

s = pptx.addSlide();
slideTitle(s, "Demo", "How to showcase the system");
bullets(s, [
  "Start Eureka, then start all microservices.",
  "Seed two driver locations through Location REST API.",
  "Create a ride through Dispatch REST API.",
  "Dispatch internally calls Location, Surge, Payment, Trip, and Notification over gRPC.",
  "Simulate driver decline and trigger reassignment.",
  "Trigger Trip SOS endpoint and show safety incident response."
], 0.95, 1.55, 11.2, 3.3, 16);
s.addText("Close: REST edge + gRPC hot path + Redis/Kafka/DB + region-local writes", { x: 0.95, y: 5.75, w: 11.2, h: 0.42, fontSize: 18, bold: true, color: C.teal, align: "center", margin: 0 });
footer(s, 11);
addNotes(s, ["Use this as the live demo checklist and closing summary."]);

await pptx.writeFile({ fileName: "deliverables/multi-region-ride-hailing-system-design.pptx" });
