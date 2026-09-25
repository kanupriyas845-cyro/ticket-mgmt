---
name: spec-driven-workflow
description: Guides spec-first development for TicketMgmtSystem. Use when planning features, writing specs, reviewing requirements, or when the user asks for spec-driven workflow.
---

# Spec-Driven Workflow

## When to Use

- Starting a new feature or epic
- Clarifying requirements before coding
- Reviewing whether implementation matches the spec

## Workflow

1. **Read context**
   - `spec/00-product-overview.md`
   - `spec/architecture.md`
   - `spec/02-conventions.md`
   - Existing specs in `spec/features/`

2. **Write or update the feature spec** in `spec/features/<feature-name>.md` using the template in `spec/features/README.md`.

3. **Get user approval** on the spec before any implementation.

4. **Implement** only what the spec describes (use `implement-from-spec` skill).

5. **Update the spec** if requirements change during implementation.

## Spec Quality Checklist

- [ ] Clear user stories or acceptance criteria
- [ ] API endpoints defined (if applicable)
- [ ] Data model / entities listed
- [ ] Edge cases and error handling noted
- [ ] Out of scope explicitly stated
- [ ] Test plan included

## Output

When drafting a spec, produce a complete markdown file ready to save under `spec/features/`.
