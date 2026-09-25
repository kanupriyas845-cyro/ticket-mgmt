# Product Overview — TicketMgmtSystem

**Status:** Draft  
**Last updated:** 2026-09-25

## Vision

A web-based ticket management system for creating, tracking, and resolving support or work tickets.

## Target Users

| Role | Description |
|------|-------------|
| **Requester** | Creates tickets, adds comments, tracks status |
| **Agent** | Works assigned tickets, updates status, resolves issues |
| **Admin** | Manages users, categories, and system settings |

## Core Capabilities (planned)

- [ ] User authentication and role-based access
- [ ] Create, view, update, and close tickets
- [ ] Assign tickets to agents
- [ ] Comment thread per ticket
- [ ] Ticket status workflow (e.g. Open → In Progress → Resolved → Closed)
- [ ] Search and filter tickets
- [ ] Dashboard with ticket metrics

## Non-Goals (initial release)

- Email ingestion / outbound notifications (future)
- SLA timers and escalation rules (future)
- Mobile native apps (web responsive only)

## Tech Stack

See `docs/project-versions.md`.

- Backend: Java 21, Spring Boot, PostgreSQL (H2 for local)
- Frontend: Next.js, React, TypeScript

## Open Questions

- [ ] Single-tenant or multi-tenant?
- [ ] OAuth / SSO or local accounts for auth?
- [ ] Ticket categories and priority levels — fixed set or configurable?
