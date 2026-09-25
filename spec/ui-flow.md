# UI Flow — TicketMgmtSystem

**Status:** Draft  
**Last updated:** 2026-09-25  
**Stack:** Next.js 15, React 19, TypeScript (`docs/project-versions.md`)  
**API:** `spec/api-contract.md` | **Status rules:** `spec/state-machine.md`

Simple, functional UI — no dashboard, auth screens, or extra chrome unless added to `requirements.md`.

---

## 1. Scope by phase

| Screen / feature | Requirements | Phase | Route |
|------------------|--------------|-------|-------|
| Ticket list | FR-03 | 1 | `/tickets` |
| Create ticket | FR-01 | 1 | `/tickets/new` |
| Ticket details | FR-02 | 1 | `/tickets/[id]` |
| Edit ticket | FR-04 | 1 | `/tickets/[id]/edit` |
| Search | — (Q-07) | 2 | `/tickets` (filter bar) |
| Status filter | — (Q-07) | 2 | `/tickets` (filter bar) |
| Status change actions | — (state-machine) | 2 | `/tickets/[id]` |
| Assignee | — (Q-05) | 3 | create, edit, details |
| Comments | — (Q-06) | 4 | `/tickets/[id]` |

Build phase 1 first. Add UI for later phases only when requirements are updated.

---

## 2. Navigation

```
Tickets (list)  →  New ticket
       ↓
   Ticket details  →  Edit
```

- App title: **Ticket Management**
- Global nav: link to **Tickets** (`/tickets`) and **New ticket** (`/tickets/new`)
- No login, profile, or settings (auth is Q-02)

---

## 3. Ticket list (`/tickets`)

### Purpose

List all tickets (FR-03). Phase 2 adds search and status filter.

### Layout

```
┌─────────────────────────────────────────────────────────┐
│  Tickets                              [+ New ticket]    │
├─────────────────────────────────────────────────────────┤
│  [ Search........................... ]  (phase 2)     │
│  Status: [All ▼] or checkboxes       (phase 2)        │
├─────────────────────────────────────────────────────────┤
│  Title          Status    Priority  Assignee  Updated   │
│  Cannot log in  OPEN      MEDIUM    Jane A.   2h ago    │
│  VPN issue      IN_PROG   HIGH      —         1d ago    │
├─────────────────────────────────────────────────────────┤
│  ← Prev   Page 1 of 3   Next →                        │
└─────────────────────────────────────────────────────────┘
```

### Behaviour

| Action | Behaviour |
|--------|-----------|
| Load | `GET /api/v1/tickets?page=0&size=20&sort=createdAt,desc` |
| Row click | Navigate to `/tickets/{id}` |
| New ticket | Navigate to `/tickets/new` |
| Pagination | Prev/next updates `page` query param |
| Empty state | Message: *No tickets yet. Create your first ticket.* + link to new |
| Loading | Show skeleton or spinner; disable filters |
| Error | Show error banner (see §10) |

### Search (phase 2)

- Single text input above the table
- Debounce **300ms**, then `GET /api/v1/tickets?search={query}&...`
- Placeholder: *Search by title or description*
- Clear button resets search

### Status filter (phase 2)

- Dropdown or multi-select: **All**, `OPEN`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`, `CANCELLED`
- On change: `GET /api/v1/tickets?status=OPEN&...` (combine with search if both set)
- **All** omits `status` param

Phase 1 list shows only: **Title**, **Created**, **Updated** (no status/priority/assignee columns until data exists).

---

## 4. Create ticket (`/tickets/new`)

### Form fields

| Field | Phase | Control | Required |
|-------|-------|---------|----------|
| Title | 1 | Text input, max 255 | Yes |
| Description | 1 | Textarea | No |
| Priority | 2 | Select: LOW, MEDIUM, HIGH (default MEDIUM) | No |
| Assignee | 3 | Select dropdown (see §9) | No |

### Actions

| Button | Behaviour |
|--------|-----------|
| **Create** | `POST /api/v1/tickets` → on **201**, redirect to `/tickets/{id}` |
| **Cancel** | Navigate back to `/tickets` |

### Client validation (before submit)

- Title not empty → inline error: *Title is required*
- Title over 255 chars → *Title must be 255 characters or less*

### Success

- Redirect to ticket details with optional toast: *Ticket created*

---

## 5. Ticket details (`/tickets/[id]`)

### Layout

```
┌─────────────────────────────────────────────────────────┐
│  ← Back to tickets                    [Edit] (phase 1) │
├─────────────────────────────────────────────────────────┤
│  Cannot log in                                          │
│  Status: OPEN    Priority: MEDIUM    Assignee: Jane A.  │
│  Created: Sep 25, 2026   Updated: Sep 25, 2026          │
├─────────────────────────────────────────────────────────┤
│  Description                                            │
│  Password reset link expired                            │
├─────────────────────────────────────────────────────────┤
│  Actions (phase 2) — only valid transitions shown:      │
│  [Start work]  [Cancel ticket]                          │
├─────────────────────────────────────────────────────────┤
│  Comments (phase 4)                                     │
│  ┌─────────────────────────────────────────────────┐   │
│  │ Jane A. · 2h ago                                │   │
│  │ Please try clearing your cache.                 │   │
│  └─────────────────────────────────────────────────┘   │
│  [ Add comment........................ ] [Post]         │
└─────────────────────────────────────────────────────────┘
```

### Load

`GET /api/v1/tickets/{id}` — **404** shows not-found page (§10).

### Status actions (phase 2)

Show **only buttons for valid transitions** from current status (`spec/state-machine.md`). Hide all others — do not rely on hiding alone; backend still returns **409** if called.

| Current status | Buttons shown |
|----------------|---------------|
| `OPEN` | **Start work** → `IN_PROGRESS`, **Cancel ticket** → `CANCELLED` |
| `IN_PROGRESS` | **Resolve** → `RESOLVED`, **Cancel ticket** → `CANCELLED` |
| `RESOLVED` | **Close** → `CLOSED` |
| `CLOSED` | None (read-only) |
| `CANCELLED` | None (read-only) |

On click: `PATCH /api/v1/tickets/{id}/status` with `{ "status": "..." }`.

- **200** → refresh ticket details
- **409** → show banner: *Cannot change status: {message from API}* (see §10)

Terminal tickets (`CLOSED`, `CANCELLED`): hide **Edit** button and comment form (phase 4).

---

## 6. Edit ticket (`/tickets/[id]/edit`)

### Form fields

Same as create (§4), pre-filled from `GET /api/v1/tickets/{id}`.

| Field | Phase |
|-------|-------|
| Title, Description | 1 |
| Priority | 2 |
| Assignee | 3 |

### Actions

| Button | Behaviour |
|--------|-----------|
| **Save** | `PATCH /api/v1/tickets/{id}` → **200**, redirect to `/tickets/{id}` |
| **Cancel** | Back to `/tickets/{id}` without saving |

### Restrictions

- If ticket is `CLOSED` or `CANCELLED`: do not show edit page; redirect to details with message *This ticket cannot be edited.*
- **409** from API → show error banner, stay on form

Status is **not** editable on this form — use status actions on details (§5).

---

## 7. Assignee (phase 3)

### Display

- **Details:** show assignee `displayName` or *Unassigned*
- **List:** assignee column or *—* when null

### Edit / create

- Dropdown: **Unassigned** (sends `assigneeId: null`) + list of users
- Populated from `GET /api/v1/users` (**assumption UI-01** — endpoint not yet in `api-contract.md`; add before phase 3)

---

## 8. Comments (phase 4)

On ticket details page, below description.

### List

- `GET /api/v1/tickets/{id}/comments?sort=createdAt,asc`
- Each comment: author name (or *Unknown*), relative time, body text
- Empty state: *No comments yet*
- Paginate if more than one page (simple prev/next)

### Add comment

- Textarea + **Post** button
- `POST /api/v1/tickets/{id}/comments` with `{ "body": "..." }`
- **201** → clear textarea, refresh list
- **400** → inline: *Comment cannot be empty*
- **409** (closed ticket) → banner: *Comments cannot be added to a closed or cancelled ticket*
- Hidden when ticket is `CLOSED` or `CANCELLED`

Comments are **read-only** after posting (no edit/delete in UI).

---

## 9. Shared UI states

Every data-fetching screen must handle:

| State | UI |
|-------|-----|
| **Loading** | Spinner or skeleton; disable submit buttons |
| **Empty** | Short message + relevant action (e.g. create link) |
| **Error** | Banner at top of content area (see §10) |
| **Not found** | Dedicated message on details: *Ticket not found* + link to list |

---

## 10. Error messages

Map API responses to user-visible text. Show API `message` when present; fall back to defaults.

### Validation (400)

Show **field-level** errors under inputs from `errors[]`:

| Field | Example message |
|-------|-----------------|
| `title` | *Title is required* / API message |
| `body` | *Comment cannot be empty* |

If no field mapping: banner *Please fix the errors below.*

### Not found (404)

| Context | Message |
|---------|---------|
| Ticket details | *Ticket not found.* |
| After save redirect | *Ticket not found. It may have been removed.* |

### Conflict (409)

| Context | Message |
|---------|---------|
| Status change | Show API `message` (e.g. *Cannot transition ticket from OPEN to RESOLVED*) |
| Edit closed ticket | *This ticket cannot be edited.* |
| Comment on closed | *Comments cannot be added to a closed or cancelled ticket.* |

### Server error (500)

| Message |
|---------|
| *Something went wrong. Please try again.* |

### Network / unreachable

| Message |
|---------|
| *Unable to reach the server. Check your connection and try again.* |

### Error banner pattern

```
┌─────────────────────────────────────────────────────────┐
│  ⚠ Cannot transition ticket from OPEN to RESOLVED  [×] │
└─────────────────────────────────────────────────────────┘
```

Dismissible. Do not use `alert()` — inline banner only.

---

## 11. What not to build

| Excluded | Reason |
|----------|--------|
| Login / register | Q-02 |
| Dashboard / charts | Not in requirements |
| Delete ticket | Not in API contract |
| Edit/delete comments | Not in API contract |
| Priority filter | Not requested (status filter only) |
| Mobile-native layout | Responsive web is enough |
| Toast library required | Optional; banner is sufficient |
| Dark mode / themes | Out of scope |

---

## 12. Page → API map

| Page | APIs used |
|------|-----------|
| List | `GET /tickets` |
| Create | `POST /tickets` |
| Details | `GET /tickets/{id}`, `PATCH /tickets/{id}/status`, `GET/POST /tickets/{id}/comments` |
| Edit | `GET /tickets/{id}`, `PATCH /tickets/{id}` |
| Assignee dropdown | `GET /users` (assumption UI-01) |

---

## 13. Assumptions

| ID | Assumption |
|----|------------|
| UI-01 | `GET /api/v1/users` returns `{ id, displayName }[]` for assignee dropdown (phase 3) |
| UI-02 | Relative times (*2h ago*) in list/details are display-only formatting |
| UI-03 | No auth headers until Q-02 is resolved |
| UI-04 | Status action buttons use confirm dialog only for **Cancel ticket** (optional simple confirm) |

---

## 14. References

- `spec/requirements.md` — FR-01–FR-04
- `spec/api-contract.md` — request/response shapes
- `spec/state-machine.md` — which status buttons to show
- `rules/20-nextjs-frontend.mdc` — code conventions
