# eLearning Management Service

A production-grade REST API for managing and retrieving eLearning component data for authenticated users, built with Java 21 and Spring Boot 3.

---

## Table of Contents

- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Setup & Installation](#setup--installation)
- [Running the Application](#running-the-application)
- [API Documentation](#api-documentation)
- [Authentication](#authentication)
- [Filtering & Pagination](#filtering--pagination)
- [Error Handling](#error-handling)
- [Logging](#logging)
- [Testing](#testing)
- [Improvements & Future Enhancements](#improvements--future-enhancements)
- [Production Considerations](#production-considerations)

---

## Tech Stack

| Category | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.4.5 |
| Persistence | Spring Data JPA + Hibernate 6 |
| Database | PostgreSQL 17 |
| Mapping | MapStruct 1.6.3 |
| Security | Spring Security (Basic Authentication) |
| Documentation | SpringDoc OpenAPI (Swagger UI) |
| Caching | Caffeine |
| Containerisation | Jib (Google), Docker Compose |
| Testing | JUnit 5, Mockito, Testcontainers |
| Build Tool | Gradle 9 |

---

## Architecture

The service follows a standard **layered architecture**:

```
HTTP Request
     ↓
Spring Security          — Basic Auth filter, AuthenticatedUser injection
     ↓
ELearningController      — routing, request/response handling, Swagger docs
     ↓
ELearningService         — business logic, caching, logging, orchestration
     ↓
UserAssignmentRepository — Spring Data JPA, optimised JPQL queries
     ↓
PostgreSQL               — persistence with indexes and constraints
```

### Domain Model

```
User (1) ──────────────── (many) UserAssignment
                                       │
ELearningComponent (1) ─────────── (many) UserAssignment

ELearningComponent (many) ──── (many) MetaTag
                             component_meta_tags (join table)
```

### Key Design Decisions

**API Versioning:**
All endpoints are versioned under `/api/v1` — protecting clients from breaking changes as the API evolves. New versions can be introduced without removing existing ones.

**Hybrid Authentication Flow:**
Spring Security authenticates the user once per request via Basic Auth, loading the full `User` entity into an `AuthenticatedUser` wrapper stored in the Security Context. Controllers inject this directly via `@AuthenticationPrincipal` — eliminating a second database query in every service method.

**Interface Projection for List Endpoint:**
The list endpoint uses a JPQL constructor expression projection selecting only 7 required columns instead of loading full entities. This avoids transferring `description` (TEXT), `metaTags`, `duration`, `category`, and audit fields not needed in the list view.

**JOIN FETCH for Detail Endpoint:**
The detail endpoint uses a single JPQL query with `JOIN FETCH` and `LEFT JOIN FETCH` to load the assignment, component, and metaTags in one database round-trip — down from 3 separate queries.

**Caffeine Caching:**
Both endpoints are cached with a 10 minute TTL and 500 entry limit. Cache keys include user ID, filter parameters, and pagination state to ensure data isolation between users.

**Database Indexes:**
Indexes on `user_id`, `component_id`, `status`, and a composite `(user_id, status)` index on `user_assignments` ensure primary query patterns use index scans rather than full table scans.

**Spring Auditing:**
`dateCreated` and `lastUpdated` are populated automatically via `@CreatedDate` and `@LastModifiedDate`. A custom `DateTimeProvider` ensures `OffsetDateTime` is used for full timezone awareness.

**Optimistic Locking:**
`UserAssignment` and `ELearningComponent` use `@Version` fields to prevent concurrent update race conditions without pessimistic locking.

---

## Prerequisites

- Java 21+
- Docker Desktop (running)
- Gradle 9+ (or use the included `./gradlew` wrapper)

---

## Setup & Installation

### 1. Clone the repository

```bash
git clone <repository-url>
cd elearning-service
```

### 2. Configure environment variables

Copy the example file and fill in your values:

```bash
cp .env.example .env
```

`.env.example`:
```env
POSTGRES_DB=elearning_service
POSTGRES_USER=your_user
POSTGRES_PASSWORD=your_password
POSTGRES_PORT=5434
APP_PORT=8080
```

> ⚠️ Never commit your `.env` file. It is listed in `.gitignore`.

---

## Running the Application

### Option A — Full Docker setup (recommended)

Build the application image with Jib, then start all services:

```bash
# Step 1 — Build the application image
./gradlew jibDockerBuild

# Step 2 — Start PostgreSQL and the application
docker compose up
```

The API will be available at `http://localhost:8080`.

> **Platform note:** The Jib build supports both `linux/amd64` and `linux/arm64`.

### Option B — Local development (app runs locally, DB in Docker)

Start only the database:

```bash
docker compose up postgres
```

Run the application with the `dev` profile:

```bash
./gradlew bootRun
```

The `dev` profile connects to `localhost:5434`, enables SQL query logging, and sets log level to `DEBUG` for full request tracing.

---

## API Documentation

Swagger UI is available at:

```
http://localhost:8080/swagger-ui/index.html
```

### API Version

Current version: **v1**

All endpoints are prefixed with `/api/v1`:

```
Base URL: /api/v1/lms/elearning-components
```

---

### Endpoints

#### `GET /api/v1/lms/elearning-components` — Get all assigned components

Returns a paginated list of all eLearning components assigned to the authenticated user. Supports optional filtering by status, type, and category.

**Query Parameters:**

| Parameter | Type | Required | Description |
|---|---|---|---|
| `status` | `BOOKED` \| `IN_PROGRESS` \| `COMPLETED` | No | Filter by assignment status |
| `type` | `COURSE` \| `MEDIA` \| `PROGRAMME` | No | Filter by component type |
| `category` | See categories below | No | Filter by component category |
| `page` | Integer | No | Page number (0-indexed, default: 0) |
| `size` | Integer | No | Page size (default: 20, max: 100) |
| `sort` | String | No | Sort field and direction (default: dateCreated,desc) |

**Available categories:**
`SOFTWARE_DEVELOPMENT`, `PROJECT_MANAGEMENT`, `AGILE_AND_SCRUM`, `DATA_SCIENCE`, `LEADERSHIP`, `COMMUNICATION`, `CYBER_SECURITY`, `CLOUD_COMPUTING`, `DESIGN_AND_UX`, `BUSINESS_ANALYSIS`

**Response `200 OK`:**
```json
{
    "content": [
        {
            "id": "550e8400-e29b-41d4-a716-446655440000",
            "name": "Introduction to Scrum",
            "type": "COURSE",
            "assignedDates": {
                "startDate": "2024-01-15",
                "endDate": "2024-06-15"
            },
            "userStatus": "BOOKED",
            "imageUrl": "http://example.com/images/scrum.jpg"
        }
    ],
    "totalElements": 1,
    "totalPages": 1,
    "size": 20,
    "number": 0
}
```

---

#### `GET /api/v1/lms/elearning-components/{componentId}` — Get component details

Returns detailed information for a specific eLearning component assigned to the authenticated user.

**Path Parameters:**

| Parameter | Type | Required | Description |
|---|---|---|---|
| `componentId` | UUID | Yes | Unique identifier of the component |

**Response `200 OK`:**
```json
{
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "name": "Introduction to Scrum",
    "description": "A comprehensive introduction to the Scrum framework...",
    "type": "COURSE",
    "availableDates": {
        "startDate": "2024-01-01",
        "endDate": "2024-12-31"
    },
    "imageUrl": "http://example.com/images/scrum.jpg",
    "metaTags": ["Scrum", "Agile", "Project Management"],
    "userStatus": "BOOKED",
    "duration": "8 hours",
    "category": "SOFTWARE_DEVELOPMENT"
}
```

**Duration format examples:**
- `"30 minutes"` — less than 1 hour
- `"2 hours"` — exact hours
- `"1h 30m"` — hours and minutes

---

### HTTP Status Codes

| Code | Meaning |
|---|---|
| `200` | Success |
| `400` | Invalid request parameter value |
| `401` | Authentication required or credentials invalid |
| `404` | Component not found or not assigned to authenticated user |
| `500` | Unexpected server error |

---

## Authentication

All endpoints require HTTP Basic Authentication.

Credentials are sent on every request via the `Authorization` header:

```
Authorization: Basic <base64(username:password)>
```

### Calling from Postman

1. Open your request in Postman
2. Click the **Authorization** tab
3. Select **Basic Auth** from the dropdown
4. Enter your `Username` and `Password`
5. Postman handles the Base64 encoding automatically

### Calling from curl

```bash
# Get all assigned components
curl -u username:password \
  http://localhost:8080/api/v1/lms/elearning-components

# Get component detail
curl -u username:password \
  http://localhost:8080/api/v1/lms/elearning-components/{componentId}
```

---

## Filtering & Pagination

### Filtering

All three filters are optional and can be combined freely:

```bash
# Filter by status
curl -u username:password \
  "http://localhost:8080/api/v1/lms/elearning-components?status=IN_PROGRESS"

# Filter by type
curl -u username:password \
  "http://localhost:8080/api/v1/lms/elearning-components?type=COURSE"

# Filter by category
curl -u username:password \
  "http://localhost:8080/api/v1/lms/elearning-components?category=SOFTWARE_DEVELOPMENT"

# Combine filters
curl -u username:password \
  "http://localhost:8080/api/v1/lms/elearning-components?status=BOOKED&type=COURSE"

# All three filters
curl -u username:password \
  "http://localhost:8080/api/v1/lms/elearning-components?status=BOOKED&type=COURSE&category=SOFTWARE_DEVELOPMENT"
```

Passing an invalid enum value returns `400 Bad Request`:

```bash
curl -u username:password \
  "http://localhost:8080/api/v1/lms/elearning-components?status=INVALID"
# → 400 Bad Request
```

### Pagination

```bash
# Page 0, 10 items per page
curl -u username:password \
  "http://localhost:8080/api/v1/lms/elearning-components?page=0&size=10"

# Sort by dateCreated ascending
curl -u username:password \
  "http://localhost:8080/api/v1/lms/elearning-components?sort=dateCreated,asc"

# Combined — filters + pagination + sort
curl -u username:password \
  "http://localhost:8080/api/v1/lms/elearning-components?status=BOOKED&page=0&size=5&sort=dateCreated,desc"
```

| Default | Value |
|---|---|
| Page size | 20 |
| Maximum page size | 100 |
| Sort field | `dateCreated` |
| Sort direction | `DESC` |

---

## Error Handling

All errors return a consistent JSON structure:

```json
{
    "message": "Component not found or not accessible",
    "timestamp": "2024-03-15T10:30:00Z"
}
```

| Scenario | Status | Message |
|---|---|---|
| Invalid parameter value (e.g. `?status=INVALID`) | `400` | Invalid value provided for one or more request parameters |
| Invalid UUID format in path | `400` | Invalid value provided for one or more request parameters |
| Missing or wrong credentials | `401` | — (Spring Security default) |
| Component not found or not assigned | `404` | Component not found or not accessible |
| Database error | `500` | A server error occurred while processing your request |
| Unexpected error | `500` | An unexpected error occurred |

> Error messages are intentionally generic to avoid exposing internal implementation details or system structure.

---

## Logging

The service uses SLF4J with Logback (Spring Boot default) across three layers.

### Log Levels by Profile

| Logger | Dev | Prod |
|---|---|---|
| `com.elearning.management` | `DEBUG` | `INFO` |
| `org.springframework.security` | `DEBUG` | `WARN` |

### What Gets Logged

| Level | Where | What |
|---|---|---|
| `DEBUG` | Service | Method entry with user ID, filter params, pagination state |
| `DEBUG` | Security | Authentication attempt and outcome per username |
| `INFO` | Service | Successful retrieval — result count and user ID |
| `WARN` | Security | Authentication failure — username not found |
| `WARN` | Exception handler | Assignment not found, invalid request parameters |
| `ERROR` | Service | Database errors with message |
| `ERROR` | Exception handler | Unexpected exceptions with full stack trace |

### What is Never Logged

```
❌ Passwords
❌ Full User objects (contain sensitive data)
❌ Raw stack traces at WARN level
✅ User IDs (UUIDs — safe identifiers)
✅ Component IDs (UUIDs — safe identifiers)
✅ Error messages (internal only, never returned to client)
```

### Sample Log Output

```
# Dev profile (DEBUG)
DEBUG c.e.m.e.service.ELearningService - Fetching assigned components for user [550e...] with filter [ComponentFilter[status=null, type=COURSE, category=null]] page [0/20]
INFO  c.e.m.e.service.ELearningService - Retrieved 5 assigned components for user [550e...]

# Auth failure (WARN)
WARN  c.e.m.e.security.CustomUserDetailsService - Authentication failed — username not found [john]

# DB error (ERROR)
ERROR c.e.m.e.service.ELearningService - Database error fetching assigned components for user [550e...]: Connection refused
```

---

## Testing

Docker must be running — integration tests use Testcontainers to spin up a real PostgreSQL 17 instance automatically.

### Run all tests

```bash
./gradlew test
```

### Test structure

```
src/test/
├── BaseIntegrationTest.java         — shared Testcontainers + Basic Auth setup
├── TestFactory.java                 — centralised test data builders
├── controller/
│   └── ELearningControllerIT.java   — full integration tests via HTTP (27 tests)
├── service/
│   └── ELearningServiceTest.java    — unit tests with Mockito (15 tests)
├── repository/
│   └── ELearningRepositoryTest.java — repository slice tests (18 tests)
└── transform/
    └── ELearningMapperTest.java     — MapStruct mapper unit tests (11 tests)
```

### Test coverage highlights

- All endpoints tested end-to-end via HTTP with real PostgreSQL
- Authentication tested — valid credentials, wrong password, wrong username, no credentials
- Data isolation verified — authenticated user only sees their own assignments
- All filter combinations tested (status, type, category, combined)
- Invalid filter values return `400` not `500`
- Pagination metadata verified (totalElements, totalPages, size)
- Component not assigned to user returns `404`
- Duration formatting verified (minutes, exact hours, hours + minutes)
- Database error handling via Mockito exception simulation
- Testcontainers used throughout — no H2, tests run against real PostgreSQL 17

### Why Testcontainers over H2

H2 uses a different SQL dialect and does not support all PostgreSQL features used in this project (enum handling, UUID types, specific index behaviour). Testcontainers starts a real PostgreSQL 17 container — matching production exactly — giving genuine confidence that what passes in tests will work in production.

---

## Improvements & Future Enhancements

| Area | Description |
|---|---|
| **Authentication** | Currently uses HTTP Basic Authentication as specified. For production, replace with JWT (JSON Web Tokens) to support token expiry, proper logout, and avoid sending credentials on every request |
| **API Versioning** | Currently at `v1`. New versions can be introduced as `/api/v2/lms/...` without removing `v1` — existing clients are unaffected |
| **Caching — Redis** | Replace Caffeine in-memory cache with Redis for distributed caching across multiple application instances |
| **Schema Migrations** | Introduce Flyway for versioned, auditable schema management instead of relying on `ddl-auto` |
| **Structured Logging** | Replace plaintext logs with JSON-structured logging (Logstash encoder) for ingestion into ELK stack or similar observability platforms |
| **Health Checks** | Add Spring Boot Actuator to expose `/actuator/health` for container orchestration and load balancer health probing |
| **UserDetails Caching** | For very high-traffic scenarios (10,000+ req/sec), UserDetails caching could be considered with strict TTLs and active invalidation on credential changes. Not implemented due to security implications of stale credential data |
| **Response Compression** | Can be enabled when deployed behind a reverse proxy (Nginx / AWS ALB) which handles GZIP compression more efficiently at the infrastructure level |
| **CI/CD Pipeline** | Add GitHub Actions or GitLab CI to run `./gradlew test` on every commit and block merges on test failure |
| **Metrics** | Integrate Micrometer + Prometheus for production visibility on cache hit rates, query latency, and request throughput |

---

## Production Considerations

**Schema management** — Replace `ddl-auto: update` with Flyway migrations before production deployment. This provides versioned, auditable, and reversible schema changes.

**Secrets management** — Environment variables are used for database credentials. In production, source these from a secrets manager (AWS Secrets Manager, HashiCorp Vault, Kubernetes Secrets).

**Container memory** — The Jib configuration sets `-XX:MaxRAMPercentage=75.0` and `-XX:+UseContainerSupport`, ensuring the JVM respects Docker memory limits and avoids OOM kills.

**Connection pooling** — HikariCP is configured with `maximum-pool-size: 10`. Tune based on expected concurrency and PostgreSQL `max_connections`. Rule of thumb: `pool_size = (number of app instances × 10) < max_connections`.

**Caching** — Caffeine in-memory cache with 10 minute TTL and 500 entry maximum. For horizontal scaling across multiple application instances, replace with Redis to share cache state.

**Database indexes** — Indexes on `user_id`, `component_id`, `status`, and composite `(user_id, status)` on `user_assignments` ensure primary query patterns use index scans at scale.

**Horizontal scaling** — The service is stateless and scales horizontally behind a load balancer. Adjust HikariCP pool size per instance to stay within PostgreSQL connection limits.

**Security** — Basic Authentication sends credentials on every request. Enforce HTTPS in production to prevent plaintext credential transmission. Consider upgrading to JWT for production deployments.

**Logging** — Set `com.elearning.management` to `INFO` in production (configured in `application-prod.yml`). Use `DEBUG` only in development to avoid log volume overhead in production.
