# Product Requirements Document (PRD)

**Project Name:** Project *Neatio Showcase* — The Fika & Kudos Ledger API

**Framework Name:** `neatio-framework` (v1.0.0)

**Target Runtime:** Open Liberty / WebSphere Liberty

**Core Technologies:** Java 21, Jakarta EE 10, MicroProfile 6.1

---

## 1. Executive Summary & Objective

Large enterprise engineering organizations face significant friction when spinning up new services: boilerplate logging, inconsistent exception structures, fragmented validation responses, and brittle observability integrations.

The goal of this project is to build a reference microservice—**The Fika & Kudos Ledger API**—to demonstrate how an internal enterprise framework (**Neatio**) built on top of **Jakarta EE 10** and **MicroProfile** eliminates repetitive plumbing, enforces company-wide standards, and improves developer ergonomics without sacrificing vendor neutrality or standard container packaging on **Open Liberty**.

---

## 2. Business Case: "Fika & Kudos Ledger"

### Domain Overview

In a distributed enterprise, teams want to send peer-to-peer appreciation tokens ("Kudos") that can be redeemed for artisan coffee, bakery treats, or charity donations at corporate hub offices.

### Functional Scope

1. **Send Kudos:** Transfer appreciation credits between employees with mandatory tags and personalized messages.
2. **Redeem Fika Voucher:** Exchange accrued kudos for internal café QR vouchers.
3. **Audit Ledger:** Query balance history and transactions per employee.

```
[ Frontend / Portal ] 
         │ 
         ▼ (HTTP / JSON)
┌─────────────────────────────────────────────────────────────┐
│  Fika Ledger App (Domain Logic & REST Endpoints)            │
│  ┌───────────────────────────────────────────────────────┐  │
│  │  Neatio Framework Layer (CDI Interceptors & Filters)  │  │
│  │  • RFC 7807 Exception Mapping                         │  │
│  │  • MDC Correlation / Tracing Propagation              │  │
│  │  • Unified i18n Bean Validation                       │  │
│  │  • Standardized Audit Logging                         │  │
│  └───────────────────────────────────────────────────────┘  │
│                                                             │
│  Jakarta EE 10 (REST, CDI, JSON-B) + MicroProfile (Metrics) │
└─────────────────────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────────┐
│  Open Liberty Runtime (server.xml, Java 21)                 │
└─────────────────────────────────────────────────────────────┘

```

---

## 3. The "Neatio" Framework: Core Capabilities

`neatio-framework` is packaged as an internal shared dependency (`se.enterprise.neatio:neatio-starter-web:1.0.0`) that automatically activates via standard CDI 4.0 Bean Archives and JAX-RS Auto-Discovery.

| Capability | Problem Solved | Neatio Abstraction | Underlying Standard |
| --- | --- | --- | --- |
| **Unified Error Format** | Inconsistent JSON errors across teams | `NeatioExceptionMapper` producing **RFC 7807 Problem Details** | Jakarta REST (`ExceptionMapper`), Jakarta JSON-B |
| **Validation Ergonomics** | Raw constraint violation arrays are ugly to parse on frontend | `@NeatioValidated` & auto-mapped `ConstraintViolationException` | Jakarta Bean Validation 3.0 |
| **Contextual Logging** | Missing correlation IDs across distributed hops | `NeatioCorrelationFilter` (propagates `X-Correlation-ID` into SLF4J MDC) | Jakarta REST Container Request/Response Filters |
| **Operation Auditing** | Manual logging of business events and execution times | `@NeatioAudited(action = "...")` interceptor | Jakarta CDI 4.0 Interceptors |
| **Config & Telemetry** | Redundant MicroProfile boilerplate in every service | Neatio default configuration profiles & pre-wired health/metrics | MicroProfile Config, Health, & Telemetry |

---

## 4. Developer Ergonomics: Before vs. After

### Scenario: Creating a Protected Transaction Endpoint

#### ❌ Without Neatio (Standard Boilerplate)

The developer must manually catch business exceptions, build response envelopes, log correlation contexts, handle transaction timing, and parse validation violations.

```java
@Path("/kudos")
@ApplicationScoped
public class KudosResource {

    private static final Logger log = LoggerFactory.getLogger(KudosResource.class);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response sendKudos(@Valid KudosTransferRequest req, @HeaderParam("X-Correlation-ID") String cid) {
        MDC.put("correlationId", cid != null ? cid : UUID.randomUUID().toString());
        long start = System.currentTimeMillis();
        try {
            log.info("Starting kudos transfer from {} to {}", req.sender(), req.recipient());
            kudosService.transfer(req);
            log.info("Kudos transferred successfully in {}ms", (System.currentTimeMillis() - start));
            return Response.ok(new GenericSuccessWrapper("Kudos sent")).build();
        } catch (InsufficientKudosBalanceException e) {
            log.warn("Balance too low: {}", e.getMessage());
            return Response.status(422).entity(new LegacyErrorDto("ERR_422", e.getMessage())).build();
        } finally {
            MDC.clear();
        }
    }
}

```

#### ✅ With Neatio (Enterprise Ergonomics)

The developer writes pure business logic. Neatio handles correlation injection, execution profiling, auditing, validation formatting, and RFC 7807 error serialization automatically.

```java
@Path("/kudos")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class KudosResource {

    @Inject
    private KudosService kudosService;

    @POST
    @NeatioAudited(action = "KUDOS_TRANSFER")
    public Response sendKudos(@Valid KudosTransferRequest req) {
        // Validation errors automatically converted to RFC 7807
        // Correlation ID auto-extracted and bound to MDC
        // InsufficientKudosBalanceException automatically mapped to 422 ProblemDetail
        var receipt = kudosService.transfer(req);
        return Response.status(Response.Status.CREATED).entity(receipt).build();
    }
}

```

---

## 5. Showcase API Specifications

### 5.1 Data Models (Java 21 Records)

```java
public record KudosTransferRequest(
    @NotBlank(message = "{kudos.sender.required}")
    String senderId,

    @NotBlank(message = "{kudos.recipient.required}")
    String recipientId,

    @Min(value = 1, message = "{kudos.amount.min}")
    @Max(value = 50, message = "{kudos.amount.max}")
    int amount,

    @Size(min = 5, max = 200, message = "{kudos.message.size}")
    String message,

    KudosCategory category
) {}

public record FikaVoucherResponse(
    UUID voucherId,
    String employeeId,
    String itemDescription,
    String qrCodePayload,
    Instant expiresAt
) {}

```

---

### 5.2 API Endpoints

```
POST /api/v1/kudos

```

* **Description:** Transfer Kudos balance between two active profiles.
* **Success (201 Created):**
```json
{
  "transactionId": "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d",
  "status": "COMPLETED",
  "timestamp": "2026-08-18T20:45:00Z"
}

```


* **Validation Failure (400 Bad Request — RFC 7807 generated by Neatio):**
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

```
POST /api/v1/fika/redeem

```

* **Description:** Redeems 10 Kudos credits for a fresh coffee & cinnamon bun voucher.
* **Direct MicroProfile Feature:** Uses `@Metered` and `@Counted` from MicroProfile Metrics directly alongside Neatio.
* **Business Rule Violation (422 Unprocessable Entity):**
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

## 6. Runtime Configuration & Liberty Packaging

### 6.1 `server.xml` (Open Liberty)

The service runs cleanly on standard MicroProfile / Jakarta EE features enabled in Liberty:

```xml
<server description="Neatio Fika Ledger Showcase Server">
    <featureManager>
        <feature>jakartaee-10.0</feature>
        <feature>microProfile-6.1</feature>
    </featureManager>

    <httpEndpoint id="defaultHttpEndpoint"
                  host="*"
                  httpPort="9080"
                  httpsPort="9443" />

    <!-- Neatio Framework Logging Output Formatter -->
    <logging traceSpecification="*=info:se.enterprise.neatio.*=all"
             consoleFormat="json"
             messageFormat="json" />

    <webApplication id="fika-ledger"
                    location="fika-ledger-api.war"
                    contextRoot="/" />
</server>

```

### 6.2 Maven Multi-Module Project Structure

```
neatio-showcase/
├── pom.xml                        # Root aggregator & dependencyManagement
├── neatio-framework/              # The enterprise starter library
│   ├── pom.xml
│   └── src/main/java/se/enterprise/neatio/
│       ├── annotation/            # @NeatioAudited, @NeatioLogged
│       ├── exception/             # RFC 7807 ProblemDetail mappers
│       ├── filter/                # Correlation ID MDC filter
│       └── interceptor/           # CDI Audit Interceptor
└── fika-ledger-service/           # The business application
    ├── pom.xml                    # Uses liberty-maven-plugin
    └── src/
        ├── main/
        │   ├── java/se/enterprise/fikavault/
        │   │   ├── RestApp.java   # @ApplicationPath("/api/v1")
        │   │   ├── boundary/      # BOUNDARY: JAX-RS Resources & DTO Records
        │   │   ├── control/       # CONTROL: Business Logic & CDI Beans
        │   │   └── entity/        # ENTITY: Domain Models & Exceptions
        │   └── resources/
        │       ├── ValidationMessages.properties
        │       └── META-INF/beans.xml
        └── main/liberty/config/
            └── server.xml

```

---

## 7. Success Criteria & KPIs

| Metric | Target | Verification Method |
| --- | --- | --- |
| **Boilerplate Reduction** | ≥ 65% fewer lines of code in REST resources | Compared against equivalent vanilla JAX-RS service |
| **Error Uniformity** | 100% of uncaught, business, and validation errors produce standard RFC 7807 JSON | Automated integration test suite running on Liberty |
| **Tracing Consistency** | 100% of logs contain `correlationId` passed from inbound request or auto-generated | Structured JSON log output inspection |
| **Deployment Time** | Fast local development cycle using Open Liberty dev mode (`mvn liberty:dev`) | Cold start under 3.5 seconds on Java 21 |

---