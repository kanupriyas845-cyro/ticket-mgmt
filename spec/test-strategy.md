# Test Strategy — TicketMgmtSystem

**Status:** Draft  
**Last updated:** 2026-09-25  
**Based on:** all `spec/` documents, `rules/testing.mdc`

Define **what** to test and **where**. Behaviour over coverage percentage. Every spec rule that can break must have a test.

---

## 1. Goals

| Goal | How |
|------|-----|
| Correct business rules | Full status transition matrix (§5) |
| API contract honoured | Controller tests assert status, JSON shape, errors |
| Data integrity | Repository / persistence tests |
| Regressions caught | One test per bug fix; parameterized tests for matrices |
| Fast feedback | Unit + slice tests default; full IT sparingly |

**Stack:** JUnit 5, Mockito, AssertJ, Spring Boot Test, H2 (`test` profile). Testcontainers + PostgreSQL only for Postgres-specific SQL tests.

---

## 2. Test layers

| Layer | Annotation / style | Class suffix | Tests |
|-------|-------------------|--------------|-------|
| **Unit** | `@ExtendWith(MockitoExtension.class)` | `*Test` | Service logic, transition rules, validation orchestration |
| **Repository** | `@DataJpaTest` | `*RepositoryTest` | Persistence, FK, queries, filters |
| **API slice** | `@WebMvcTest` + `MockMvc` | `*ControllerTest` | HTTP status, JSON, error bodies |
| **Integration** | `@SpringBootTest` + `MockMvc` | `*IT` | Cross-layer flows (search, comments thread) |

**Rule:** Do not duplicate the same assertion at unit and IT level. Unit for logic; API slice for HTTP contract; IT for persistence + API together.

**Frontend:** Out of scope for this document unless E2E is added later. Backend tests are the source of truth for business rules (`spec/ui-flow.md` mirrors API).

---

## 3. Alignment with implementation phases

| Phase | Spec areas to test |
|-------|-------------------|
| **1** | Ticket CRUD, validation, 404, persistence of core fields |
| **2** | Status transitions (full matrix), search, status filter, priority |
| **3** | Assignee FK, unassign, assignee filter |
| **4** | Comments list/create, cascade, closed-ticket rules |

---

## 4. Test catalogue

### 4.1 Ticket CRUD (phase 1)

| ID | Behaviour | Layer | Expected |
|----|-----------|-------|----------|
| T-01 | Create ticket with title + description | API + IT | **201**, body has `id`, `title`, `createdAt` |
| T-02 | Create ticket defaults to `OPEN` status | Service + IT | `status` = `OPEN` (phase 2 field) |
| T-03 | Get ticket by id | API | **200**, full `TicketResponse` |
| T-04 | Get non-existent ticket | API | **404**, error JSON shape |
| T-05 | List tickets paginated | API + IT | **200**, page wrapper, default sort `createdAt,desc` |
| T-06 | List empty | API | **200**, `content: []`, `totalElements: 0` |
| T-07 | Update title and description | API + IT | **200**, fields persisted |
| T-08 | PATCH with empty body | API | **400** |
| T-09 | POST/PATCH rejects `status` in body | API | **400** (`state-machine.md` §7) |

### 4.2 Validation (all phases)

| ID | Behaviour | Layer | Expected |
|----|-----------|-------|----------|
| V-01 | Create with blank title | API | **400**, `errors[].field` = `title` |
| V-02 | Create with title > 255 chars | API | **400**, field error on `title` |
| V-03 | Create with invalid priority code | API | **400** (phase 2) |
| V-04 | Status change with missing `status` | API | **400** |
| V-05 | Status change with unknown code `FOO` | API | **400**, field `status` |
| V-06 | Comment with blank body | API | **400**, field `body` (phase 4) |
| V-07 | Invalid `page` / `size` on list | API | **400** |
| V-08 | PATCH assigneeId unknown user | API | **404** (phase 3) |

Assert error JSON: `status`, `error`, `message`, `timestamp`; validation includes `errors[]`.

### 4.3 API errors (non-validation)

| ID | Behaviour | Layer | Expected |
|----|-----------|-------|----------|
| E-01 | Ticket not found on GET/PATCH/status | API | **404**, `message` contains id |
| E-02 | Invalid status transition | API + Service | **409**, message format §5 `state-machine.md` |
| E-03 | Edit `CLOSED` ticket | API | **409** (per `api-contract.md`) |
| E-04 | Comment on `CLOSED`/`CANCELLED` ticket | API | **409** (phase 4) |
| E-05 | Same-status transition (no-op) | API + Service | **409**, *already in status* |

Global handler: one test verifying `InvalidTicketStatusTransitionException` maps to **409** with correct body.

### 4.4 Search (phase 2)

| ID | Behaviour | Layer | Expected |
|----|-----------|-------|----------|
| S-01 | Search matches title (case-insensitive) | Repository + API | Ticket returned |
| S-02 | Search matches description | Repository + API | Ticket returned |
| S-03 | Search no match | API | **200**, empty `content` |
| S-04 | Search + pagination | API | Correct `totalElements`, page content |
| S-05 | Empty search param | API | Same as unfiltered list |

### 4.5 Filtering (phase 2–3)

| ID | Behaviour | Layer | Expected |
|----|-----------|-------|----------|
| F-01 | Filter by single status `OPEN` | Repository + API | Only OPEN tickets |
| F-02 | Filter by multiple statuses | API | Union of matches |
| F-03 | Filter by priority `HIGH` | API | Only HIGH (phase 2) |
| F-04 | Filter by `assigneeId` | API | Only that assignee (phase 3) |
| F-05 | Filter `unassigned=true` | API | Only tickets with null assignee (phase 3) |
| F-06 | Combine search + status filter | API | Intersection of both |
| F-07 | Invalid status code in filter | API | **400** |

### 4.6 Comments (phase 4)

| ID | Behaviour | Layer | Expected |
|----|-----------|-------|----------|
| C-01 | List comments for ticket | API + IT | **200**, ordered `createdAt,asc` |
| C-02 | List comments ticket not found | API | **404** |
| C-03 | Add comment | API + IT | **201**, body persisted |
| C-04 | Add comment empty body | API | **400** |
| C-05 | Add comment to closed ticket | API | **409** |
| C-06 | List comments paginated | API | Page wrapper correct |
| C-07 | Delete ticket cascades comments | Repository | Comments removed (`data-model.md`) |

### 4.7 Database persistence

| ID | Behaviour | Layer | Expected |
|----|-----------|-------|----------|
| P-01 | Save and load ticket by id | `@DataJpaTest` | Fields match |
| P-02 | `title` NOT NULL constraint | `@DataJpaTest` | Constraint violation on null title |
| P-03 | FK ticket → status | `@DataJpaTest` | Cannot insert invalid `status_id` |
| P-04 | FK ticket → priority | `@DataJpaTest` | Cannot insert invalid `priority_id` |
| P-05 | FK ticket → assignee nullable | `@DataJpaTest` | Null assignee allowed; invalid id fails |
| P-06 | Status change persisted after transition | IT | GET after PATCH shows new status |
| P-07 | Update ticket updates `updated_at` | `@DataJpaTest` or IT | Timestamp changes |
| P-08 | Comment FK to ticket | `@DataJpaTest` | Orphan comment insert fails |
| P-09 | Custom query: search by title | `@DataJpaTest` | Query returns expected rows |
| P-10 | Custom query: filter by status | `@DataJpaTest` | Query returns expected rows |

Use H2 for all persistence tests unless PostgreSQL-specific behaviour is required.

---

## 5. Status transition matrix (complete)

Authoritative rules: `spec/state-machine.md`.  
**Every cell below must have at least one test** (service unit + API slice recommended for valid; parameterized batch for invalid).

### 5.1 Valid transitions — must return **200**

| From | To | Test name example |
|------|-----|-------------------|
| `OPEN` | `IN_PROGRESS` | `shouldTransitionOpenToInProgress` |
| `OPEN` | `CANCELLED` | `shouldTransitionOpenToCancelled` |
| `IN_PROGRESS` | `RESOLVED` | `shouldTransitionInProgressToResolved` |
| `IN_PROGRESS` | `CANCELLED` | `shouldTransitionInProgressToCancelled` |
| `RESOLVED` | `CLOSED` | `shouldTransitionResolvedToClosed` |

After success: assert response `status`, DB row updated (IT).

### 5.2 Invalid transitions — must return **409**

| From | To | Category |
|------|-----|----------|
| `OPEN` | `OPEN` | No-op |
| `OPEN` | `RESOLVED` | Skips step |
| `OPEN` | `CLOSED` | Skips step |
| `IN_PROGRESS` | `OPEN` | Backwards |
| `IN_PROGRESS` | `IN_PROGRESS` | No-op |
| `IN_PROGRESS` | `CLOSED` | Skips step |
| `RESOLVED` | `OPEN` | Backwards |
| `RESOLVED` | `IN_PROGRESS` | Backwards |
| `RESOLVED` | `RESOLVED` | No-op |
| `RESOLVED` | `CANCELLED` | Wrong path |
| `CLOSED` | `OPEN` | Terminal |
| `CLOSED` | `IN_PROGRESS` | Terminal |
| `CLOSED` | `RESOLVED` | Terminal |
| `CLOSED` | `CLOSED` | Terminal + no-op |
| `CLOSED` | `CANCELLED` | Terminal |
| `CANCELLED` | `OPEN` | Terminal |
| `CANCELLED` | `IN_PROGRESS` | Terminal |
| `CANCELLED` | `RESOLVED` | Terminal |
| `CANCELLED` | `CLOSED` | Terminal |
| `CANCELLED` | `CANCELLED` | Terminal + no-op |

**Total: 5 valid + 20 invalid = 25 transition pairs.**

### 5.3 Parameterized test template

```java
// Service: expect exception
@ParameterizedTest
@CsvSource({
    "OPEN, OPEN",
    "OPEN, RESOLVED",
    "CLOSED, IN_PROGRESS",
    "CANCELLED, CLOSED"
    // ... all 20 invalid rows
})
void shouldRejectInvalidTransition(TicketStatus from, TicketStatus to) { ... }

// API: expect 409
@ParameterizedTest
@CsvSource({ /* same rows */ })
void shouldReturn409ForInvalidTransition(TicketStatus from, TicketStatus to) { ... }
```

### 5.4 Additional status tests

| ID | Behaviour | Expected |
|----|-----------|----------|
| ST-01 | Status change on missing ticket | **404** |
| ST-02 | Invalid enum in request body | **400** |
| ST-03 | Create ticket always `OPEN` | Service asserts initial status |
| ST-04 | Sequential valid path OPEN→…→CLOSED | IT walks full happy path |

---

## 6. Suggested test classes

```
backend/src/test/java/com/ticketmgmt/
├── service/
│   ├── TicketServiceTest.java          # CRUD logic, assignee, edit rules
│   └── TicketStatusServiceTest.java    # Full transition matrix (§5)
├── controller/
│   ├── TicketControllerTest.java       # CRUD + search/filter API
│   ├── TicketStatusControllerTest.java # Status endpoint + errors
│   └── TicketCommentControllerTest.java
├── repository/
│   ├── TicketRepositoryTest.java       # Persistence, search/filter queries
│   └── TicketCommentRepositoryTest.java
└── integration/
    ├── TicketApiIT.java                # End-to-end CRUD + persistence
    ├── TicketSearchFilterIT.java       # Search + filter combined
    └── TicketCommentIT.java            # Comment flow + cascade
```

---

## 7. Test data conventions

| Item | Approach |
|------|----------|
| Seed statuses/priorities | `@Sql` or test `ApplicationRunner` loading lookup rows |
| Tickets per test | Factory/builder method in test source; unique titles |
| Isolation | `@Transactional` rollback on IT, or `@BeforeEach` cleanup |
| IDs | Use saved entity ids — no hardcoded `1` unless seeded |

---

## 8. What not to test

| Skip | Reason |
|------|--------|
| Spring Framework internals | Trust the framework |
| Getter/setters with no logic | No behaviour |
| Only asserting `isOk()` without body | Meaningless |
| Every duplicate invalid transition at IT + unit + repo | Pick layers per §2 |
| Frontend pixel layout | No UI test spec yet |
| Auth 401/403 | Until Q-02 resolved — add tests when auth lands |

---

## 9. Running tests

```bash
cd backend
./gradlew test          # all tests
./gradlew test --tests '*TicketStatus*'   # single class/pattern
```

CI (when added): run `./gradlew test` on every PR. Fail build on any failure.

---

## 10. Traceability

| Spec | Test sections |
|------|---------------|
| `requirements.md` FR-01–FR-04 | §4.1 |
| `api-contract.md` | §4.1–4.6, §4.3 |
| `state-machine.md` | §5 (full matrix) |
| `data-model.md` | §4.7 |
| `ui-flow.md` | §4.3–4.6 (API behaviour UI depends on) |

Use `/generate-tests` command to scaffold tests from feature specs. Use `/review-code` to verify test coverage before merge.

---

## 11. References

- `rules/testing.mdc` — coding conventions for tests
- `commands/generate-tests.md` — test generation workflow
- `spec/state-machine.md` — transition authority
