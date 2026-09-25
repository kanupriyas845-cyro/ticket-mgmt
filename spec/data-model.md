# Data Model — TicketMgmtSystem

**Status:** Draft  
**Last updated:** 2026-09-25  
**Based on:** `spec/requirements.md`, `spec/architecture.md`

---

## 1. Requirements alignment

| Entity / field | In `requirements.md` today? | Notes |
|----------------|----------------------------|-------|
| **Ticket** (title, description, timestamps) | **Yes** — FR-01–FR-04, assumption A-01 | Implement first |
| **Status** | **No** — listed out of scope; Q-04 open | Schema proposed below; do not implement until requirements updated |
| **Priority** | **No** — listed out of scope; Q-01 open | Schema proposed below; do not implement until requirements updated |
| **Assignee** | **No** — listed out of scope; Q-05 open | Requires minimal `users` table; do not implement until requirements updated |
| **Comments** | **No** — listed out of scope; Q-06 open | Schema proposed below; do not implement until requirements updated |

This document defines the **full target schema** for the entities named above. Only the **tickets** core columns (§3.1) are mandated by current requirements. Everything else is **proposed** pending `requirements.md` approval.

---

## 2. Entity relationship diagram

```
┌─────────────────┐       ┌─────────────────┐
│  ticket_status  │       │ ticket_priority │
│─────────────────│       │─────────────────│
│ id (PK)         │       │ id (PK)         │
│ code (UNIQUE)   │       │ code (UNIQUE)   │
│ label           │       │ label           │
│ sort_order      │       │ sort_order      │
└────────┬────────┘       └────────┬────────┘
         │                         │
         │ 1                       │ 1
         │                         │
         ▼ *                       ▼ *
┌────────────────────────────────────────────┐
│                  tickets                   │
│────────────────────────────────────────────│
│ id (PK)                                    │
│ title                                      │
│ description                                │
│ status_id (FK) ────────────────────────────┤
│ priority_id (FK) ──────────────────────────┤
│ assignee_id (FK, nullable) ───┐            │
│ created_at                     │            │
│ updated_at                     │            │
└────────────────────────────────┼────────────┘
         │ 1                     │
         │                       │ *
         ▼ *              ┌──────┴──────┐
┌─────────────────┐      │    users    │
│ ticket_comments │      │─────────────│
│─────────────────│      │ id (PK)     │
│ id (PK)         │      │ display_name│
│ ticket_id (FK)  │      │ email (UQ)  │
│ author_id (FK)  │──────│ created_at  │
│ body            │      │ updated_at  │
│ created_at      │      └─────────────┘
└─────────────────┘
```

---

## 3. Tables

### 3.1 `tickets` — **required by requirements**

| Column | Type | Constraints | Notes |
|--------|------|-------------|-------|
| `id` | `BIGINT` | PK, auto-increment | AD-04 in architecture: Long assumed until UUID decided |
| `title` | `VARCHAR(255)` | NOT NULL | Assumption A-01 |
| `description` | `TEXT` | NULL allowed | Assumption A-01 |
| `created_at` | `TIMESTAMP WITH TIME ZONE` | NOT NULL | Set on insert |
| `updated_at` | `TIMESTAMP WITH TIME ZONE` | NOT NULL | Set on insert/update |

**Indexes:** `idx_tickets_created_at` on `created_at` (supports list ordering).

#### Proposed columns (not in requirements yet)

Add only after `requirements.md` is updated:

| Column | Type | Constraints | Notes |
|--------|------|-------------|-------|
| `status_id` | `SMALLINT` | NOT NULL, FK → `ticket_status.id` | Default `OPEN` at insert |
| `priority_id` | `SMALLINT` | NOT NULL, FK → `ticket_priority.id` | Default `MEDIUM` at insert |
| `assignee_id` | `BIGINT` | NULL, FK → `users.id` | Nullable — unassigned tickets allowed |

**Indexes (when proposed columns added):** `idx_tickets_status_id`, `idx_tickets_assignee_id`.

---

### 3.2 `ticket_status` — **proposed**

Lookup table for ticket lifecycle state. Transition rules live in **application code** (service layer), not DB constraints.

| Column | Type | Constraints | Notes |
|--------|------|-------------|-------|
| `id` | `SMALLINT` | PK | Surrogate key |
| `code` | `VARCHAR(32)` | NOT NULL, UNIQUE | Machine name: `OPEN`, `IN_PROGRESS`, … |
| `label` | `VARCHAR(64)` | NOT NULL | Display name |
| `sort_order` | `SMALLINT` | NOT NULL | UI ordering |

**Seed data (assumption — confirm via Q-04):**

| code | label | sort_order |
|------|-------|------------|
| `OPEN` | Open | 1 |
| `IN_PROGRESS` | In Progress | 2 |
| `RESOLVED` | Resolved | 3 |
| `CLOSED` | Closed | 4 |
| `CANCELLED` | Cancelled | 5 |

> Valid/invalid transitions are enforced in the **service layer**. See `spec/state-machine.md`.

---

### 3.3 `ticket_priority` — **proposed**

| Column | Type | Constraints | Notes |
|--------|------|-------------|-------|
| `id` | `SMALLINT` | PK | |
| `code` | `VARCHAR(32)` | NOT NULL, UNIQUE | `LOW`, `MEDIUM`, `HIGH` |
| `label` | `VARCHAR(64)` | NOT NULL | |
| `sort_order` | `SMALLINT` | NOT NULL | Higher = more urgent in UI |

**Seed data (assumption — confirm via Q-01):**

| code | label | sort_order |
|------|-------|------------|
| `LOW` | Low | 1 |
| `MEDIUM` | Medium | 2 |
| `HIGH` | High | 3 |

---

### 3.4 `users` — **proposed** (supports assignee)

Minimal user record for assignment. **Not** a full auth model — authentication is Q-02.

| Column | Type | Constraints | Notes |
|--------|------|-------------|-------|
| `id` | `BIGINT` | PK, auto-increment | |
| `display_name` | `VARCHAR(128)` | NOT NULL | Shown in UI as assignee name |
| `email` | `VARCHAR(255)` | NOT NULL, UNIQUE | Assumed unique identifier; auth TBD |
| `created_at` | `TIMESTAMP WITH TIME ZONE` | NOT NULL | |
| `updated_at` | `TIMESTAMP WITH TIME ZONE` | NOT NULL | |

**Indexes:** unique on `email` (implicit from UNIQUE constraint).

> No `password` or `role` columns until auth (Q-02) and roles (Q-03) are defined in requirements.

---

### 3.5 `ticket_comments` — **proposed**

| Column | Type | Constraints | Notes |
|--------|------|-------------|-------|
| `id` | `BIGINT` | PK, auto-increment | |
| `ticket_id` | `BIGINT` | NOT NULL, FK → `tickets.id` | |
| `author_id` | `BIGINT` | NULL, FK → `users.id` | Nullable if author unknown in v1 |
| `body` | `TEXT` | NOT NULL | Comment text |
| `created_at` | `TIMESTAMP WITH TIME ZONE` | NOT NULL | No `updated_at` — comments are immutable |

**Indexes:** `idx_ticket_comments_ticket_id` on `ticket_id`.

---

## 4. Relationships

| From | To | Cardinality | FK column | On delete |
|------|----|-------------|-----------|-----------|
| `tickets` | `ticket_status` | many → one | `status_id` | RESTRICT |
| `tickets` | `ticket_priority` | many → one | `priority_id` | RESTRICT |
| `tickets` | `users` (assignee) | many → one | `assignee_id` | SET NULL |
| `ticket_comments` | `tickets` | many → one | `ticket_id` | CASCADE |
| `ticket_comments` | `users` (author) | many → one | `author_id` | SET NULL |

**Rationale:**

- **RESTRICT** on status/priority — cannot delete a lookup row still referenced by tickets.
- **SET NULL** on assignee/author — deleting a user unassigns tickets / anonymises comment author rather than blocking delete.
- **CASCADE** on comments — deleting a ticket removes its comments (ticket owns the thread).

---

## 5. Important constraints (summary)

| Rule | Enforcement |
|------|-------------|
| Ticket title required, max 255 chars | DB `NOT NULL` + validation on DTO |
| Comment body required | DB `NOT NULL` + validation on DTO |
| Status/priority codes unique | DB `UNIQUE` on lookup tables |
| User email unique | DB `UNIQUE` |
| Assignee optional | `assignee_id` nullable |
| Status transitions | **Service layer only** — not DB triggers |
| No orphan comments | FK `ticket_id` NOT NULL |
| Timestamps in UTC | Application writes `Instant` / `OffsetDateTime` |

---

## 6. JPA entity mapping (names only — no code)

| Table | Entity class | Package |
|-------|--------------|---------|
| `tickets` | `Ticket` | `com.ticketmgmt.entity` |
| `ticket_status` | `TicketStatus` | `com.ticketmgmt.entity` |
| `ticket_priority` | `TicketPriority` | `com.ticketmgmt.entity` |
| `users` | `User` | `com.ticketmgmt.entity` |
| `ticket_comments` | `TicketComment` | `com.ticketmgmt.entity` |

---

## 7. Implementation order

| Phase | Tables | Driven by |
|-------|--------|-----------|
| **1** | `tickets` (core columns only) | FR-01–FR-04, A-01 |
| **2** | `ticket_status`, `ticket_priority` + FKs on `tickets` | Requirements update + Q-01, Q-04 |
| **3** | `users` + `assignee_id` on `tickets` | Requirements update + Q-05 |
| **4** | `ticket_comments` | Requirements update + Q-06 |

---

## 8. Open questions (from requirements)

| ID | Affects |
|----|---------|
| Q-01 | Priority levels, additional ticket fields |
| Q-04 | Status values and allowed transitions |
| Q-05 | Whether assignee is required, reassign rules |
| Q-06 | Comment editing, deletion, author requirements |
| Q-02 / Q-03 | Whether `users` needs auth/role columns |

---

## 9. References

- `spec/requirements.md` — authoritative scope
- `spec/architecture.md` — persistence approach
- `spec/02-conventions.md` — naming conventions
- `rules/testing.mdc` — state transition test expectations (when Q-04 resolved)
