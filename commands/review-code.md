# Review Code

Review AI-generated or recent code changes. Be **critical** — find real problems. Do not rubber-stamp.

## Scope

1. Identify what to review: unstaged/staged diff, named files, or the most recent changes if not specified.
2. Read the relevant feature spec in `spec/features/` if the change maps to a feature.
3. Cross-check against project rules:
   - `rules/java-springboot.mdc`
   - `rules/api-standards.mdc`
   - `rules/testing.mdc`
   - `spec/02-conventions.md`

## Review checklist

Work through each area. **Skip sections that don't apply**, but never skip an area just because the diff is small.

### 1. Correctness
- Does the logic match the spec and acceptance criteria?
- Edge cases handled (null, empty lists, not found, duplicate)?
- Ticket status transitions valid/invalid per `rules/testing.mdc`?
- Race conditions or transaction boundaries wrong?

### 2. Requirements
- Implements only what was asked — no scope creep?
- Missing behaviour from the spec?
- Breaking changes not documented?

### 3. Security
- Hardcoded secrets, tokens, or credentials?
- SQL injection, mass assignment, or auth bypass?
- Sensitive data in logs or error responses?
- Missing auth/role checks on endpoints?

### 4. Validation
- Input validated at API boundary (`@Valid`, Jakarta annotations)?
- Business rules enforced in service layer, not just controller?
- Invalid input returns 400 with field errors per `rules/api-standards.mdc`?

### 5. Errors
- Correct HTTP status codes (404, 409, etc.)?
- Consistent error JSON shape — no stack traces leaked?
- Exceptions caught and mapped in `@RestControllerAdvice`?
- Failures not silently swallowed?

### 6. Tests
- Tests exist for new/changed behaviour?
- Happy path **and** error paths covered?
- State transition tests for valid **and** invalid moves?
- Tests assert meaningful outcomes (not just status 200)?
- Missing tests for bugs that are easy to introduce?

### 7. Unnecessary code
- Dead code, unused imports, commented-out blocks?
- Over-abstraction (extra layers, helpers used once)?
- Duplicated logic that should be consolidated?
- Docs or comments that restate obvious code?

## How to report

**Do not say "looks good" unless you verified every checklist item and found nothing.**

Use this format:

```
## Summary
One sentence: merge-ready / needs fixes / major issues.

## Critical (must fix)
- [file:line] Issue — why it matters — suggested fix

## Should fix
- [file:line] Issue — why it matters — suggested fix

## Minor / optional
- [file:line] Suggestion

## Missing
- What is absent (tests, validation, spec update, etc.)

## Verified OK
- Brief list of what you checked and found correct (keep short)
```

If there are no issues in a severity tier, write **"None found"** for that tier — do not omit the tier.

## Rules for the reviewer

- Cite **file paths and line numbers** when possible.
- Every finding must be **specific and actionable** — no vague "consider improving error handling".
- Prefer showing what's wrong over praising what's right.
- Do **not** rewrite the code unless the user asks — review only.
- If you cannot verify something (e.g. no tests run), say so explicitly.
