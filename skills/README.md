# Skills

Reusable agent workflows for this project. Each skill is a directory with a `SKILL.md` file.

Cursor discovers these via `.cursor/skills` → `skills/` symlink.

## Available Skills

| Skill | Trigger |
|-------|---------|
| `spec-driven-workflow` | Spec-first planning, reviewing specs, aligning work to requirements |
| `implement-from-spec` | Implementing a feature that already has an approved spec |
| `documentation` | Writing project docs — decisions, setup, API behaviour, assumptions |

## Adding a Skill

1. Create `skills/<skill-name>/SKILL.md` with YAML frontmatter (`name`, `description`).
2. Keep `SKILL.md` under 500 lines; use `reference.md` for detail.
3. Set `disable-model-invocation: true` for explicit-only skills (slash-command style).
