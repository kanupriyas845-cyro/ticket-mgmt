# API Contract — TicketMgmtSystem

**Status:** Draft  
**Last updated:** 2026-09-25  
**Base URL:** `/api/v1`  
**Based on:** `spec/requirements.md`, `spec/data-model.md`, `spec/02-conventions.md`

All requests and responses use **JSON**. Timestamps are **ISO-8601 UTC**.

---

## 1. Requirements alignment

| API group | In `requirements.md` today? | Phase |
|-----------|----------------------------|-------|
| Create / list / view / update ticket | **Yes** — FR-01–FR-04 | **1** |
| Status change | **No** — Q-04 | **2** |
| Comments | **No** — Q-06 | **4** |
| Search & filters on list | **No** — Q-07 | **2** |
| Priority / assignee on ticket | **No** — Q-01, Q-05 | **2–3** |

Endpoints are defined here as the **target contract**. Implement in phase order; do not build phase 2+ until `requirements.md` is updated.

---

## 2. Shared schemas

### 2.1 Error (non-validation)

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Ticket not found with id: 42",
  "timestamp": "2026-09-25T10:00:00Z"
}
```

### 2.2 Validation error (400)

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "timestamp": "2026-09-25T10:00:00Z",
  "errors": [
    { "field": "title", "message": "must not be blank" }
  ]
}
```

### 2.3 Page wrapper (lists)

```json
{
  "content": [ ],
  "page": 0,
  "size": 20,
  "totalElements": 142,
  "totalPages": 8
}
```

### 2.4 TicketResponse (full)

Used for single-ticket GET, create, update, and status change responses.

```json
{
  "id": 1,
  "title": "Cannot log in",
  "description": "Password reset link expired",
  "status": "OPEN",
  "priority": "MEDIUM",
  "assignee": {
    "id": 5,
    "displayName": "Jane Agent"
  },
  "createdAt": "2026-09-25T10:00:00Z",
  "updatedAt": "2026-09-25T10:00:00Z"
}
```

| Field | Phase | Notes |
|-------|-------|-------|
| `id`, `title`, `description`, `createdAt`, `updatedAt` | 1 | Required by FR-01–FR-04 |
| `status`, `priority` | 2 | Omitted in phase 1 responses |
| `assignee` | 3 | `null` when unassigned |

### 2.5 TicketSummaryResponse (list item)

Lighter shape for list results:

```json
{
  "id": 1,
  "title": "Cannot log in",
  "status": "OPEN",
  "priority": "MEDIUM",
  "assignee": { "id": 5, "displayName": "Jane Agent" },
  "createdAt": "2026-09-25T10:00:00Z",
  "updatedAt": "2026-09-25T10:00:00Z"
}
```

Phase 1 list items omit `status`, `priority`, `assignee`.

### 2.6 CommentResponse

```json
{
  "id": 10,
  "ticketId": 1,
  "body": "Please try again after clearing cache.",
  "author": {
    "id": 5,
    "displayName": "Jane Agent"
  },
  "createdAt": "2026-09-25T11:00:00Z"
}
```

`author` may be `null` if unknown (data-model assumption).

### 2.7 Status / priority codes (enums in JSON)

| Type | Values |
|------|--------|
| Status | `OPEN`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`, `CANCELLED` |
| Priority | `LOW`, `MEDIUM`, `HIGH` |

---

## 3. Tickets

### 3.1 Create ticket

| | |
|---|---|
| **Method** | `POST` |
| **URL** | `/api/v1/tickets` |
| **Phase** | 1 |

**Request**

```json
{
  "title": "Cannot log in",
  "description": "Password reset link expired"
}
```

| Field | Type | Required | Rules |
|-------|------|----------|-------|
| `title` | string | yes | max 255, not blank |
| `description` | string | no | |

**Phase 2+ optional fields** (when requirements updated):

```json
{
  "title": "Cannot log in",
  "description": "Password reset link expired",
  "priority": "HIGH",
  "assigneeId": 5
}
```

| Field | Type | Required | Rules |
|-------|------|----------|-------|
| `priority` | string | no | Defaults to `MEDIUM` |
| `assigneeId` | number | no | Must reference existing user (phase 3) |

`status` is **not** set on create — server defaults to `OPEN` (phase 2).

**Response — 201 Created**

- Body: `TicketResponse`
- Header: `Location: /api/v1/tickets/{id}`

**Errors**

| Status | When |
|--------|------|
| 400 | Validation failed (blank title, title too long, invalid priority code) |
| 404 | `assigneeId` references non-existent user (phase 3) |
| 401 | Not authenticated (when auth added — Q-02) |
| 500 | Unexpected server error |

---

### 3.2 List tickets (with search & filters)

| | |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/v1/tickets` |
| **Phase** | 1 (basic list); **2** (search & filters) |

**Query parameters**

| Param | Type | Phase | Description |
|-------|------|-------|-------------|
| `page` | int | 1 | Zero-based page index. Default `0` |
| `size` | int | 1 | Page size. Default `20`, max `100` |
| `sort` | string | 1 | `createdAt,desc` (default) or `updatedAt,desc` |
| `search` | string | 2 | Case-insensitive match on **title** or **description** |
| `status` | string | 2 | Filter by status code. Repeatable: `status=OPEN&status=IN_PROGRESS` **or** comma-separated `status=OPEN,IN_PROGRESS` (pick one style at implementation) |
| `priority` | string | 2 | Filter by priority code |
| `assigneeId` | number | 3 | Filter by assignee; use `unassigned=true` for no assignee (assumption) |

**Assumption:** `unassigned` query param (`true`/`false`) filters tickets with no assignee. Not in requirements — confirm at implementation.

**Response — 200 OK**

```json
{
  "content": [ { /* TicketSummaryResponse */ } ],
  "page": 0,
  "size": 20,
  "totalElements": 42,
  "totalPages": 3
}
```

**Errors**

| Status | When |
|--------|------|
| 400 | Invalid `page`/`size`, unknown `sort` field, invalid status/priority code in filter |
| 401 | Not authenticated (when auth added) |
| 500 | Unexpected server error |

---

### 3.3 View ticket

| | |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/v1/tickets/{ticketId}` |
| **Phase** | 1 |

**Path parameters**

| Param | Type | Description |
|-------|------|-------------|
| `ticketId` | long | Ticket ID |

**Response — 200 OK**

- Body: `TicketResponse`

**Errors**

| Status | When |
|--------|------|
| 404 | Ticket not found |
| 401 | Not authenticated (when auth added) |
| 500 | Unexpected server error |

---

### 3.4 Update ticket

Updates **title**, **description**, **priority**, and **assignee** only. Status changes use §4.

| | |
|---|---|
| **Method** | `PATCH` |
| **URL** | `/api/v1/tickets/{ticketId}` |
| **Phase** | 1 (title/description); **2–3** (priority/assignee) |

**Request**

```json
{
  "title": "Updated title",
  "description": "Updated description"
}
```

All fields optional (partial update). At least one field must be present.

**Phase 2+ fields:**

```json
{
  "title": "Updated title",
  "priority": "HIGH",
  "assigneeId": 5
}
```

| Field | Type | Rules |
|-------|------|-------|
| `title` | string | max 255, not blank if provided |
| `description` | string | |
| `priority` | string | Valid priority code |
| `assigneeId` | number | `null` to unassign (assumption) |

**Response — 200 OK**

- Body: `TicketResponse`

**Errors**

| Status | When |
|--------|------|
| 400 | Empty body, validation failure, invalid priority |
| 404 | Ticket not found; `assigneeId` user not found |
| 409 | Update not allowed in current status (assumption — e.g. cannot edit `CLOSED` ticket; confirm in feature spec) |
| 401 / 403 | Auth / permission (when added) |
| 500 | Unexpected server error |

---

## 4. Status changes

Status is changed via a **dedicated endpoint**, not the general PATCH on the ticket.

### 4.1 Change ticket status

| | |
|---|---|
| **Method** | `PATCH` |
| **URL** | `/api/v1/tickets/{ticketId}/status` |
| **Phase** | 2 |

**Request**

```json
{
  "status": "IN_PROGRESS"
}
```

| Field | Type | Required | Rules |
|-------|------|----------|-------|
| `status` | string | yes | Target status code |

**Allowed transitions** — authoritative rules in `spec/state-machine.md`:

| From | To |
|------|-----|
| `OPEN` | `IN_PROGRESS` |
| `OPEN` | `CANCELLED` |
| `IN_PROGRESS` | `RESOLVED` |
| `IN_PROGRESS` | `CANCELLED` |
| `RESOLVED` | `CLOSED` |

All other transitions → **409 Conflict** (see `state-machine.md` §4 for invalid examples).

**Response — 200 OK**

- Body: `TicketResponse` with updated `status`

**Errors**

| Status | When |
|--------|------|
| 400 | Missing/invalid `status` value |
| 404 | Ticket not found |
| 409 | Invalid transition (e.g. `CLOSED` → `OPEN`, `OPEN` → `RESOLVED`) |
| 401 / 403 | Auth / permission (when added) |
| 500 | Unexpected server error |

**Example 409**

```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Cannot transition ticket from CLOSED to IN_PROGRESS",
  "timestamp": "2026-09-25T10:00:00Z"
}
```

---

## 5. Comments

Comments are **immutable** (no update/delete in this contract — assumption per data-model).

### 5.1 List comments on a ticket

| | |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/v1/tickets/{ticketId}/comments` |
| **Phase** | 4 |

**Query parameters**

| Param | Type | Default | Description |
|-------|------|---------|-------------|
| `page` | int | `0` | Zero-based page |
| `size` | int | `20` | Page size, max `100` |
| `sort` | string | `createdAt,asc` | Oldest first |

**Response — 200 OK**

```json
{
  "content": [ { /* CommentResponse */ } ],
  "page": 0,
  "size": 20,
  "totalElements": 3,
  "totalPages": 1
}
```

**Errors**

| Status | When |
|--------|------|
| 400 | Invalid pagination params |
| 404 | Ticket not found |
| 401 | Not authenticated (when auth added) |
| 500 | Unexpected server error |

---

### 5.2 Add comment to a ticket

| | |
|---|---|
| **Method** | `POST` |
| **URL** | `/api/v1/tickets/{ticketId}/comments` |
| **Phase** | 4 |

**Request**

```json
{
  "body": "Please try again after clearing cache."
}
```

| Field | Type | Required | Rules |
|-------|------|----------|-------|
| `body` | string | yes | not blank |

`author` is set server-side from authenticated user (assumption). If no auth (Q-02), `author` may be `null`.

**Response — 201 Created**

- Body: `CommentResponse`
- Header: `Location: /api/v1/tickets/{ticketId}/comments/{commentId}`

**Errors**

| Status | When |
|--------|------|
| 400 | Blank `body` |
| 404 | Ticket not found |
| 409 | Cannot comment on `CLOSED` ticket (assumption — confirm Q-06) |
| 401 | Not authenticated (when auth added) |
| 500 | Unexpected server error |

---

## 6. Endpoint summary

| Method | URL | Purpose | Phase |
|--------|-----|---------|-------|
| `POST` | `/api/v1/tickets` | Create ticket | 1 |
| `GET` | `/api/v1/tickets` | List tickets (search & filters) | 1 / 2 |
| `GET` | `/api/v1/tickets/{ticketId}` | View ticket | 1 |
| `PATCH` | `/api/v1/tickets/{ticketId}` | Update ticket fields | 1 |
| `PATCH` | `/api/v1/tickets/{ticketId}/status` | Change status | 2 |
| `GET` | `/api/v1/tickets/{ticketId}/comments` | List comments | 4 |
| `POST` | `/api/v1/tickets/{ticketId}/comments` | Add comment | 4 |

---

## 7. Assumptions (not in requirements)

| ID | Assumption |
|----|------------|
| AC-01 | New tickets default to `OPEN` status and `MEDIUM` priority (phase 2) |
| AC-02 | `assigneeId: null` on PATCH unassigns the ticket |
| AC-03 | `unassigned=true` query param filters tickets with no assignee |
| AC-04 | `CLOSED` tickets cannot be edited (409 on PATCH) — confirm in feature spec |
| AC-05 | Comments cannot be added to `CLOSED` tickets (409) — confirm Q-06 |
| AC-06 | Comments are create-only (no PUT/PATCH/DELETE) |
| AC-07 | Auth (`401`/`403`) deferred until Q-02/Q-03 |

---

## 8. Implementation order

Matches `spec/data-model.md` §7:

1. **Phase 1** — §3.1, §3.2 (no filters), §3.3, §3.4 (title/description only)
2. **Phase 2** — §3.2 filters/search, §4.1, §3.1/3.4 priority fields
3. **Phase 3** — assignee on ticket + `assigneeId` filter
4. **Phase 4** — §5 comments

---

## 9. References

- `spec/requirements.md` — scope
- `spec/data-model.md` — entities and constraints
- `spec/02-conventions.md` — naming and error shape
- `rules/api-standards.mdc` — coding standards
- `rules/testing.mdc` — transition and API test expectations
