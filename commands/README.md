# Commands

Cursor slash commands for this project. Each `.md` file becomes a `/command-name` in the Cursor chat.

Loaded via `.cursor/commands` → `commands/` symlink.

## Available Commands

| Command | File | Purpose |
|---------|------|---------|
| `/plan-feature` | `plan-feature.md` | Draft a feature spec without implementing |
| `/new-feature` | `new-feature.md` | Create spec + scaffold plan for a new feature |
| `/review-spec` | `review-spec.md` | Pre-implementation spec review — gaps, ambiguity, contradictions |
| `/review-code` | `review-code.md` | Critical review of AI-generated or recent code changes |
| `/generate-tests` | `generate-tests.md` | Generate behaviour-focused tests from a feature spec |

## Adding a Command

1. Create `commands/<name>.md` (lowercase, hyphens).
2. First line: `# Title` (shown in command picker).
3. Body: clear step-by-step instructions for the agent.
