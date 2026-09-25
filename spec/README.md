# Specifications

**Source of truth** for what this project builds. Code follows specs; specs do not follow code.

## Structure

| File / Folder | Purpose |
|---------------|---------|
| `requirements.md` | **Authoritative** user requirements, assumptions, and open questions |
| `00-product-overview.md` | Product vision, users, core capabilities |
| `architecture.md` | System design, components, data flow |
| `data-model.md` | Database tables, relationships, constraints |
| `api-contract.md` | REST API endpoints, request/response shapes, errors |
| `state-machine.md` | Ticket status transitions and backend enforcement rules |
| `ui-flow.md` | Frontend pages, flows, and error handling |
| `test-strategy.md` | What to test, transition matrix, layer mapping |
| `01-architecture.md` | Redirect → `architecture.md` |
| `02-conventions.md` | API, naming, and coding conventions |
| `features/` | One markdown file per feature |

## Workflow

1. Write or update a spec **before** implementation.
2. Get user approval on the spec.
3. Implement using `skills/implement-from-spec/`.
4. Update the spec if requirements change.

## Feature Spec Naming

`spec/features/<kebab-case-name>.md` — e.g. `spec/features/user-authentication.md`
