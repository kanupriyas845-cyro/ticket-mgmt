# Rules

Cursor agent rules for this project. Files use `.mdc` format with YAML frontmatter.

Cursor loads these automatically via `.cursor/rules` → `rules/` symlink.

## Files

| Rule | Scope | Description |
|------|-------|-------------|
| `00-project-core.mdc` | Always | Spec-driven workflow, version pins, safety defaults |
| `java-springboot.mdc` | `backend/**/*.java` | Java 21 / Spring Boot standards |
| `api-standards.mdc` | `backend/**/controller`, `backend/**/dto` | REST API URLs, DTOs, status codes, errors |
| `testing.mdc` | `backend/src/test/**` | Unit, integration, API, and state-transition tests |
| `20-nextjs-frontend.mdc` | `frontend/**` | Next.js / React conventions |

## Adding a Rule

1. Create `rules/<name>.mdc` with frontmatter (`description`, `globs` or `alwaysApply`).
2. Keep rules under 50 lines; one concern per file.
3. Prefer actionable guidance over generic advice.
