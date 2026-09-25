# Review Spec

Review a spec **before coding**. Find gaps and problems — do not edit any files during this review.

## Scope

1. Ask which spec to review, or use the most recent in `spec/features/` if not specified.
2. Read the target spec and cross-check against:
   - `spec/00-product-overview.md`
   - `spec/architecture.md`
   - `spec/02-conventions.md`
   - `rules/api-standards.mdc` (API shape and error format)
   - `rules/testing.mdc` (state transitions, test expectations)
   - Related specs in `spec/features/` for conflicts

## Review checklist

Work through each area. Flag anything that would block or confuse implementation.

### 1. Missing requirements
- User stories or acceptance criteria incomplete?
- Roles/permissions defined (requester, agent, admin)?
- Out-of-scope section present and sufficient?
- Dependencies on other features noted?

### 2. Unclear or ambiguous
- Vague language ("handle appropriately", "as needed")?
- Undefined terms or enums?
- Open questions left unanswered?
- Acceptance criteria not testable?

### 3. Contradictions
- Conflicts with product overview or architecture doc?
- Conflicts with another feature spec?
- API conventions inconsistent with `spec/02-conventions.md`?
- Status values or roles named differently across docs?

### 4. Missing edge cases
- Empty/null inputs, not found, duplicate, concurrent updates?
- Who can do what when a resource is in a given state?
- What happens on partial failure?
- Pagination, sorting, filtering for list endpoints?

### 5. API gaps
- Endpoints missing for stated user stories?
- Request/response shapes undefined?
- HTTP methods and status codes specified (201, 404, 409, etc.)?
- Error responses defined for failure paths?
- Auth required per endpoint?

### 6. State machine gaps (tickets and workflows)
- All statuses listed?
- Valid transitions defined?
- Invalid transitions and expected error (409) defined?
- Terminal states identified (e.g. CLOSED)?
- Who can trigger each transition?

### 7. Test plan
- Test plan section present?
- Covers happy path, validation errors, and state transition failures?
- Enough detail to write tests without guessing?

## How to report

**Do not edit spec files. Do not implement code. Review only.**

**Do not say "ready to implement" unless every checklist item was checked.**

```
## Summary
One sentence: ready to code / needs clarification / not ready.

## Blockers (must resolve before coding)
- Issue — which doc/section — suggested clarification

## Gaps
- Missing requirement, API, edge case, or transition — where it should be defined

## Ambiguities
- Unclear statement — why it's ambiguous — question to answer

## Contradictions
- Conflict between doc A and doc B — what conflicts

## Minor / optional
- Nice-to-have clarifications

## Ready
- Brief list of what is well defined (keep short)
```

If a section has no findings, write **"None found"** — do not skip sections.

## Rules for the reviewer

- Reference **specific sections** in the spec, not vague "the API section".
- Every finding must be **actionable** — state what question to answer or what to add.
- Suggest what to add, but **do not apply edits** — the user updates the spec.
- If reviewing a Draft spec, expect more gaps; still report them honestly.
- Compare ticket workflows against `rules/testing.mdc` minimum transition table.
