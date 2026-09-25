# Agent Instructions — TicketMgmtSystem

This project uses a **spec-driven** workflow. Read this file first, then follow the linked instructions.

## Workflow

1. **Read the spec** before implementing (`spec/`).
2. **Follow project rules** (`rules/` — also loaded via `.cursor/rules/`).
3. **Use pinned versions** from `docs/project-versions.md`.
4. **Do not build** unless the user explicitly asks.
5. **Update specs** when requirements change, before changing code.

## Directory Map

| Folder | Purpose |
|--------|---------|
| `spec/` | Product requirements, architecture, feature specs (source of truth) |
| `rules/` | Cursor agent rules (coding standards, constraints) |
| `skills/` | Reusable agent workflows (spec-driven dev, feature implementation) |
| `commands/` | Slash commands for common tasks (`/new-feature`, etc.) |
| `docs/` | Project documentation (versions, setup, ADRs) |
| `.specstory/history/` | Auto-saved agent session history (SpecStory) |

## Tech Stack (summary)

See `docs/project-versions.md` for full details.

- **Backend:** Java 21, Spring Boot 4.1.1, Gradle wrapper, PostgreSQL 17 / H2
- **Frontend:** Next.js 15.5.x, React 19, TypeScript 5.7, Node 20 LTS

## Before Implementing a Feature

1. Check `spec/features/` for an existing spec.
2. If missing, use `/plan-feature` or `/new-feature` command.
3. Get user approval on the spec before writing code.
4. Implement only what the spec describes.

## Key Spec Files

- `spec/00-product-overview.md` — what we are building
- `spec/architecture.md` — system design
- `spec/02-conventions.md` — naming, API, and code conventions
