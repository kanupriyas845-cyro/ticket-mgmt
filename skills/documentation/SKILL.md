---
name: documentation
description: Writes and updates TicketMgmtSystem project docs for decisions, setup, API behaviour, and assumptions. Use when documenting the project, writing setup guides, ADRs, or API notes — not for restating obvious code.
---

# Documentation

## Principle

Document **why** and **what to know**, not **what the code already says**.

Skip docs that only repeat class names, method signatures, or self-explanatory logic.

## What belongs where

| Location | Purpose |
|----------|---------|
| `spec/` | Requirements, feature behaviour, acceptance criteria |
| `docs/` | How to run, configure, and operate the project |
| `docs/adr/` | Architecture and design **decisions** with rationale |
| `docs/api.md` | API overview and non-obvious behaviour (not a full endpoint dump) |
| Code comments | Only non-obvious business rules or tricky edge cases |

Do not duplicate content across `spec/` and `docs/`. Link between them instead.

## When to write docs

| Trigger | Document |
|---------|----------|
| Toolchain or env setup changes | Update `docs/setup.md` |
| Non-obvious choice (auth, DB, state machine) | New `docs/adr/NNN-title.md` |
| API has surprising rules (409 on bad transition) | `docs/api.md` or the feature spec |
| Assumption that future devs could get wrong | ADR or a short note in the relevant doc |
| Version pin changes | Update `docs/project-versions.md` |

## When **not** to write docs

- Every new class or endpoint by default
- Restating what a well-named method already expresses
- Generic framework tutorials (link to official docs instead)
- Commenting every line of code

## ADR format

Save as `docs/adr/001-short-title.md`:

```markdown
# ADR 001: <Title>

**Status:** Accepted | Superseded  
**Date:** YYYY-MM-DD

## Context
What problem or choice prompted this?

## Decision
What we chose.

## Consequences
Trade-offs, what becomes easier/harder.
```

Number sequentially. One decision per file.

## Setup doc (`docs/setup.md`)

Cover only what a new developer needs:

- Prerequisites (Java 21, Node 20, Docker/Postgres)
- How to start backend and frontend
- Environment variables (names only — no secret values)
- Common troubleshooting

Point to `docs/project-versions.md` for exact versions.

## API doc (`docs/api.md`)

Document **behaviour**, not a generated reference:

- Base URL and auth approach
- Pagination and error format (link to `spec/02-conventions.md`)
- Non-obvious rules (e.g. ticket status transitions → 409)
- Resources that need extra explanation

Full endpoint lists belong in OpenAPI/Swagger when the backend exists — do not hand-maintain a duplicate.

## Writing style

- Short sentences. Bullet lists over long paragraphs.
- Use tables for status codes, env vars, or state transitions.
- Include **examples** only when behaviour is not obvious from the name.
- Date significant updates at the top of the file.

## Checklist before finishing

- [ ] Would a new developer learn something they couldn't get from reading the code?
- [ ] Is this the right folder (`spec/` vs `docs/`)?
- [ ] Are links to related specs/ADRs included?
- [ ] No secrets, credentials, or real URLs with tokens
