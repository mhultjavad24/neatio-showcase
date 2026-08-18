# Project Neatio Showcase — The Fika & Kudos Ledger API

A reference microservice implementation demonstrating how the **Neatio Framework** (`neatio-framework`) on **Jakarta EE 10** and **MicroProfile 6.1** running on **Open Liberty (Java 21)** eliminates repetitive boilerplate, standardizes error envelopes to RFC 7807 Problem Details, propagates MDC correlation IDs, and enforces enterprise developer ergonomics.

---

## 🏛 Architecture Overview

```
neatio-showcase/
├── pom.xml                                   # Root Maven aggregator (Java 21, dependencyManagement)
├── neatio-framework/                         # Enterprise starter library (se.enterprise.neatio)
│   ├── src/main/java/se/enterprise/neatio/
│   │   ├── annotation/                       # @NeatioAudited, @NeatioLogged
│   │   ├── exception/                        # ProblemDetail, NeatioExceptionMapper, ConstraintViolationExceptionMapper
│   │   ├── filter/                           # NeatioCorrelationFilter (X-Correlation-ID + SLF4J MDC)
│   │   ├── interceptor/                      # NeatioAuditInterceptor, NeatioLoggingInterceptor
│   │   └── model/                            # ProblemDetail, InvalidParameter, AuditEvent
│   └── src/test/java/                        # Comprehensive framework unit test suite
│
└── fika-ledger-service/                      # The business microservice (se.enterprise.fikavault)
    ├── src/main/java/se/enterprise/fikavault/
    │   ├── RestApp.java                      # @ApplicationPath("/api/v1")
    │   ├── api/                              # KudosResource, FikaResource, LedgerResource
    │   ├── domain/                           # Business logic, repositories, models, exceptions
    │   └── health/                           # MP Health @Liveness and @Readiness checks
    ├── src/main/resources/
    │   ├── ValidationMessages.properties     # Bean Validation message bundles (default & Swedish)
    │   └── META-INF/microprofile-config.properties
    ├── src/main/liberty/config/server.xml    # Open Liberty configuration (jakartaee-10.0, microProfile-6.1)
    └── src/test/java/                        # Unit & pipeline verification tests
```

---

## 🚀 Key Framework Features

| Capability | Neatio Abstraction | Standard Specification |
|---|---|---|
| **RFC 7807 Problem Details** | `NeatioExceptionMapper` & `ProblemDetail` | Jakarta REST `ExceptionMapper`, Jakarta JSON-B |
| **Unified Validation Errors** | `ConstraintViolationExceptionMapper` | Jakarta Bean Validation 3.0 |
| **Contextual Tracing / MDC** | `NeatioCorrelationFilter` & `CorrelationContext` | Jakarta REST Container Filters, SLF4J MDC |
| **Structured Auditing** | `@NeatioAudited(action = "...")` | Jakarta CDI 4.0 Interceptors |
| **Health & Metrics** | `@Liveness`, `@Readiness`, `@Timed`, `@Counted` | MicroProfile Health 4.0 & Metrics 5.1 |

---

## 📦 API Endpoints

### 1. Send Kudos (`POST /api/v1/kudos`)
Transfers Kudos appreciation credits between employees.

```bash
curl -X POST http://localhost:9080/api/v1/kudos \
  -H "Content-Type: application/json" \
  -H "X-Correlation-ID: c83b4b8c-572f-4882-93cb-3392305a415b" \
  -d '{
    "senderId": "SE-1001",
    "recipientId": "SE-1002",
    "amount": 10,
    "message": "Outstanding work on the architecture!",
    "category": "CRAFTSMANSHIP"
  }'
```

**Response (201 Created):**
```json
{
  "transactionId": "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d",
  "status": "COMPLETED",
  "timestamp": "2026-08-18T20:45:00Z"
}
```

---

### 2. Redeem Fika Voucher (`POST /api/v1/fika/redeem`)
Exchanges 10 Kudos credits for an artisan coffee and cinnamon bun QR voucher.

```bash
curl -X POST http://localhost:9080/api/v1/fika/redeem \
  -H "Content-Type: application/json" \
  -d '{"employeeId": "SE-1001"}'
```

**Response (200 OK):**
```json
{
  "voucherId": "f5090fa5-a27e-40bd-a9d6-80d59636b921",
  "employeeId": "SE-1001",
  "itemDescription": "Artisan Coffee & Fresh Cinnamon Bun",
  "qrCodePayload": "FIKA-QR-f5090fa5-a27e-40bd-a9d6-80d59636b921-SE-1001-1787090341",
  "expiresAt": "2026-08-21T20:45:00Z"
}
```

---

### 3. Business Rule Error (HTTP 422 - RFC 7807)
When employee `SE-9821` has only 4 credits:

```json
{
  "type": "https://neatio.internal/errors/insufficient-balance",
  "title": "Insufficient Kudos Balance",
  "status": 422,
  "detail": "Employee 'SE-9821' has 4 credits, but 10 are required for a Fika Voucher.",
  "instance": "/api/v1/fika/redeem",
  "correlationId": "c83b4b8c-572f-4882-93cb-3392305a415b"
}
```

---

### 4. Validation Failure (HTTP 400 - RFC 7807)
When input constraints fail:

```json
{
  "type": "https://neatio.internal/errors/validation-failed",
  "title": "Constraint Violation",
  "status": 400,
  "detail": "Input payload failed validation rules",
  "instance": "/api/v1/kudos",
  "correlationId": "c83b4b8c-572f-4882-93cb-3392305a415b",
  "invalidParameters": [
    {
      "field": "amount",
      "rejectedValue": 0,
      "message": "Kudos transfer must be at least 1 credit."
    }
  ]
}
```

---

### 5. Query Ledger Summary (`GET /api/v1/ledger/{employeeId}`)

```bash
curl http://localhost:9080/api/v1/ledger/SE-9821
```

---

## 🛠 Build & Run

### Build & Run All Tests
```bash
mvn clean package
```

### Run on Open Liberty in Dev Mode
```bash
mvn -pl fika-ledger-service liberty:dev
```
Open Liberty will start on `http://localhost:9080`.
- OpenAPI UI: `http://localhost:9080/openapi/ui`
- MicroProfile Health: `http://localhost:9080/health`
- MicroProfile Metrics: `http://localhost:9080/metrics`
