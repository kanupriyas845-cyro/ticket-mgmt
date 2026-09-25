# Architecture — TicketMgmtSystem

**Status:** Draft  
**Last updated:** 2026-09-25  
**Based on:** `spec/requirements.md`

A **simple monolithic** web application. One backend, one database, one frontend. No microservices or extra infrastructure unless a future requirement demands it.

---

## 1. System context

```
┌──────────────┐         REST (JSON)          ┌──────────────────┐
│   Browser    │ ◄──────────────────────────► │  Next.js (React) │
└──────────────┘                              │    frontend/     │
                                              └────────┬─────────┘
                                                       │ HTTP
                                                       ▼
                                              ┌──────────────────┐
                                              │  Spring Boot API │
                                              │     backend/     │
                                              └────────┬─────────┘
                                                       │ JDBC
                                                       ▼
                                              ┌──────────────────┐
                                              │  H2  /  Postgres │
                                              └──────────────────┘
```

| Component | Role |
|-----------|------|
| **Frontend** | UI for creating, viewing, listing, and updating tickets (FR-01–FR-04) |
| **Backend** | REST API, business logic, persistence |
| **Database** | Stores ticket data |

**Not in scope:** microservices, API gateway, Kafka, Redis, message queues, search engines, or separate worker services. Add only when `requirements.md` says they are needed.

---

## 2. Architectural style

| Decision | Choice | Rationale |
|----------|--------|-----------|
| Style | **Modular monolith** | Single deployable backend; simple to develop and run locally |
| API | **REST over HTTP/JSON** | Matches requirements assumption A-02; fits Spring Boot + Next.js |
| Communication | **Synchronous request/response** | Sufficient for CRUD ticket operations |
| Tenancy | **Single-tenant** | Requirements assumption A-05 |
| Auth | **TBD** | Requirements Q-02 — not designed until confirmed |

---

## 3. Repository layout

```
TicketMgmtSystem/
├── backend/                 # Spring Boot application
│   ├── src/main/java/
│   │   └── com/ticketmgmt/
│   │       ├── config/
│   │       ├── controller/
│   │       ├── service/
│   │       ├── repository/
│   │       ├── entity/
│   │       ├── dto/
│   │       └── exception/
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── application-{local,dev,prod}.yml
│   ├── src/test/
│   └── build.gradle         # Gradle wrapper at backend/ or repo root (TBD)
├── frontend/                # Next.js application
│   ├── src/
│   │   ├── app/             # App Router pages
│   │   ├── components/
│   │   └── lib/api/         # Backend API client
│   └── package.json
├── spec/
├── docs/
└── ...
```

---

## 4. Backend

### Stack

| Layer | Technology |
|-------|------------|
| Runtime | Java 21 |
| Framework | Spring Boot 4.1.1 |
| Web | Spring Web (REST controllers) |
| Persistence | Spring Data JPA / Hibernate |
| Validation | Jakarta Bean Validation |
| Build | Gradle wrapper (assumption A-03; see Q-08) |

Versions: `docs/project-versions.md`.

### Layering

```
HTTP Request
    → Controller   (parse input, return ResponseEntity)
    → Service        (business logic, transactions)
    → Repository     (database access)
    → Entity         (JPA mapping)
```

- API base path: `/api/v1/`
- DTOs for all request/response bodies — entities never exposed over the wire
- Global exception handler for consistent error JSON (`rules/api-standards.mdc`)

### Configuration profiles

| Profile | Database | Use |
|---------|----------|-----|
| `local` | H2 (in-memory) | Fast local development |
| `test` | H2 (in-memory) | Automated tests |
| `dev` / `prod` | PostgreSQL 17 | Staging and production-like environments |

Switch via `spring.profiles.active`. Connection details from environment variables — not hardcoded.

### Initial domain model (v1)

Based on requirements FR-01–FR-04 and assumption A-01. Expand only after Q-01 is answered.

```
Ticket
├── id            (Long or UUID — decide at scaffold)
├── title         (required)
├── description   (optional text)
├── createdAt     (timestamp)
└── updatedAt     (timestamp)
```

No status, assignee, comments, or categories until added to `requirements.md`.

---

## 5. Frontend

### Stack

| Layer | Technology |
|-------|------------|
| Framework | Next.js 15.5.x (App Router) |
| UI library | React 19 |
| Language | TypeScript 5.7 |
| Runtime | Node 20 LTS |

### Responsibilities

- Render ticket list, detail, create, and edit pages
- Call backend REST API via a shared client in `src/lib/api/`
- Handle loading, error, and empty states in the UI

### Communication

- Browser → Next.js: server and client components as appropriate
- Next.js → Spring Boot: `fetch` (or thin wrapper) to `BACKEND_URL/api/v1/...`
- CORS enabled on backend for the frontend origin in dev

Auth flow (headers, cookies, tokens) is **deferred** until Q-02 is resolved.

---

## 6. Data flow (example: create ticket)

```
1. User fills form in Next.js
2. Frontend POST /api/v1/tickets  { title, description }
3. Controller validates CreateTicketRequest (@Valid)
4. Service creates Ticket entity, saves via Repository
5. Service returns TicketResponse DTO
6. Controller responds 201 + JSON body
7. Frontend navigates to ticket detail or list
```

Same synchronous pattern for GET (list/detail) and PATCH (update).

---

## 7. Cross-cutting concerns

| Concern | Approach |
|---------|----------|
| Validation | Jakarta annotations on DTOs; 400 with field errors |
| Errors | `@RestControllerAdvice`; shape per `spec/02-conventions.md` |
| Logging | SLF4J / Logback (Spring Boot default) |
| CORS | Spring `WebMvcConfigurer` or `application.yml` — dev origins only |
| Migrations | Flyway or Liquibase (decide at scaffold; prefer one, not both) |
| API docs | Springdoc OpenAPI (optional, add at scaffold if useful) |

---

## 8. Deployment (high level)

Single backend JAR + single Next.js build. No orchestration required for v1.

| Environment | Backend | Frontend | Database |
|-------------|---------|----------|----------|
| Local | `bootRun` on :8080 | `npm run dev` on :3000 | H2 |
| Production | JAR behind reverse proxy | Static/Node host or Vercel-like | PostgreSQL |

Containerisation (Docker) is optional for local Postgres — not required for architecture v1.

---

## 9. Explicitly excluded

These are **not** part of the architecture unless requirements change:

| Technology | Reason |
|------------|--------|
| Microservices | Unnecessary complexity for v1 scope |
| Kafka / RabbitMQ | No async messaging requirement |
| Redis | No caching/session requirement yet |
| Elasticsearch | No search requirement (Q-07 open) |
| Separate auth server | Auth not confirmed (Q-02) |
| GraphQL | REST is sufficient |
| WebSockets | No real-time requirement |

---

## 10. Open architecture decisions

Tied to `spec/requirements.md` §5. Do not implement until resolved.

| ID | Decision | Impact |
|----|----------|--------|
| **AD-01** | Authentication mechanism | Security filter chain, frontend token/cookie handling |
| **AD-02** | Gradle at repo root vs `backend/` only | Project structure |
| **AD-03** | Flyway vs Liquibase | Migration file layout |
| **AD-04** | Ticket ID type (Long vs UUID) | Entity and API design |
| **AD-05** | Next.js env var for backend URL | `NEXT_PUBLIC_API_URL` pattern |

---

## 11. Decision log

| Date | Decision | Rationale |
|------|----------|-----------|
| 2026-09-25 | Modular monolith | Requirements TR-08/09; keep it simple |
| 2026-09-25 | REST + JSON | Assumption A-02; standard for Spring + Next.js |
| 2026-09-25 | H2 local / Postgres elsewhere | TR-03, assumption A-04 |
| 2026-09-25 | No Redis/Kafka/microservices | No requirement; avoid premature complexity |

---

## 12. References

- `spec/requirements.md` — what we build
- `spec/02-conventions.md` — API and naming conventions
- `docs/project-versions.md` — pinned versions
- `rules/java-springboot.mdc` — backend coding standards
- `rules/api-standards.mdc` — REST API standards
