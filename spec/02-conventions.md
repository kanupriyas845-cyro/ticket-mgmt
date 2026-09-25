# Conventions — TicketMgmtSystem

**Status:** Draft  
**Last updated:** 2026-09-25

## Naming

| Item | Convention | Example |
|------|------------|---------|
| Feature specs | kebab-case | `user-authentication.md` |
| REST paths | kebab-case, plural resources | `/api/v1/tickets` |
| Java packages | lowercase | `com.ticketmgmt.service` |
| Java classes | PascalCase | `TicketService` |
| DB tables | snake_case | `ticket_comments` |
| React components | PascalCase | `TicketList.tsx` |
| TS files / folders | kebab-case | `ticket-list/` |

## API Conventions

- Version prefix: `/api/v1/`
- JSON request/response bodies
- ISO-8601 timestamps in UTC
- Paginated lists: `?page=0&size=20&sort=createdAt,desc`
- Standard error shape:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Human-readable message",
  "timestamp": "2026-09-25T10:00:00Z"
}
```

## HTTP Status Codes

| Code | Usage |
|------|-------|
| 200 | Successful GET, PUT, PATCH |
| 201 | Successful POST (created) |
| 204 | Successful DELETE |
| 400 | Validation error |
| 401 | Unauthenticated |
| 403 | Forbidden (wrong role) |
| 404 | Resource not found |
| 409 | Conflict (e.g. duplicate) |
| 500 | Unexpected server error |

## Git Conventions

- Branch: `cursor/<ticket>-<short-summary>` or `feature/<name>`
- Commits: conventional commits (`feat:`, `fix:`, `docs:`, `chore:`)

## Spec Conventions

- One feature per file in `spec/features/`
- Include acceptance criteria as checkboxes
- Mark status: Draft → Approved → Implemented
