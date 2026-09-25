---
name: implement-from-spec
description: Implements a TicketMgmtSystem feature from an approved spec in spec/features/. Use when the user asks to build or implement a feature that has a spec, or after spec approval.
disable-model-invocation: true
---

# Implement From Spec

## Prerequisites

- An approved feature spec exists at `spec/features/<feature-name>.md`
- User has explicitly asked to implement (not just plan)

## Steps

1. **Read the spec** end to end. List acceptance criteria as a checklist.

2. **Read constraints**
   - `docs/project-versions.md`
   - Relevant rules in `rules/`
   - `spec/02-conventions.md`

3. **Plan the implementation** (brief): files to create/modify, API changes, tests.

4. **Implement incrementally**
   - Backend first if API is needed, then frontend
   - One acceptance criterion at a time
   - Match existing patterns; minimal scope

5. **Verify**
   - Run relevant tests and lint
   - Walk through acceptance criteria from the spec

6. **Update docs** if the spec or architecture changed.

## Do Not

- Implement features without an approved spec
- Expand scope beyond the spec without user approval
- Change pinned versions without user approval
