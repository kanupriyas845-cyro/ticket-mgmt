# Generate Tests

Generate tests from an approved feature spec. Test **behaviour**, not line coverage.

## Scope

1. Identify the feature spec in `spec/features/` (ask if unclear).
2. Read acceptance criteria, API section, data model, edge cases, and test plan from the spec.
3. Read existing implementation if present — only add tests for specified behaviour.
4. Follow `rules/testing.mdc`, `rules/api-standards.mdc`, and `spec/02-conventions.md`.

Do **not** generate tests for unimplemented features unless the user asks to scaffold them ahead of code.

## What to generate (by layer)

Derive test cases **from the spec**, not from scanning every public method.

| Layer | Class suffix | When |
|-------|--------------|------|
| Service unit | `*ServiceTest` | Business rules, state transitions, domain errors |
| Repository | `*RepositoryTest` | Custom queries, unique constraints, persistence |
| Controller API | `*ControllerTest` | HTTP status, JSON body, validation errors |
| Full flow | `*IT` | Only when spec requires cross-layer behaviour |

**One test = one behaviour.** Name methods: `shouldReturn409_whenTransitionFromClosedToOpen`.

## Required coverage areas

### 1. Acceptance criteria → happy path
- One test per acceptance criterion where it maps to code behaviour.
- Assert **outcomes** (returned data, side effects), not just "no exception".

### 2. State transitions (tickets / workflows)
From the spec's state machine section:

- **Every valid transition** — service unit test + API test if exposed via REST.
- **Every invalid transition** — service throws conflict → API returns **409**.
- **Terminal states** — no further transitions allowed.

Use `@ParameterizedTest` when the spec lists many transition pairs.

### 3. Validation
- Missing required fields → **400** + `errors[]` with field names.
- Invalid formats (size, email, enum) → **400**.
- Service unit tests for rules that go beyond bean validation.

### 4. API errors
- **404** — resource not found.
- **401 / 403** — auth/role restrictions from spec.
- **409** — conflicts (duplicate, invalid state).
- Assert error JSON shape: `status`, `error`, `message`, `timestamp` (and `errors` for 400).

### 5. Persistence
- `@DataJpaTest` for: save/load, unique constraints, cascade behaviour, custom `@Query`.
- Verify DB state after operations when the spec requires it (e.g. status persisted after transition).

### 6. Edge cases from spec
- Empty lists, pagination boundaries, not-found IDs, duplicate creates.
- Role-based access per state (who can transition what).
- Only cases **listed or implied** in the spec — do not invent requirements.

## What not to generate

- Tests that only call a method and assert non-null.
- Duplicate tests at unit and integration layer for the same behaviour.
- Tests for getters/setters, constructors, or framework wiring.
- `@Disabled` placeholder tests.
- Tests for behaviour not described in the spec (flag as "spec gap" instead).

## Workflow

1. **Extract test cases** from spec into a short list before writing code:

```
| # | Behaviour | Layer | Type |
|---|-----------|-------|------|
| 1 | Create ticket returns 201 | Controller | happy |
| 2 | Blank title returns 400 | Controller | validation |
| 3 | CLOSED → OPEN returns 409 | Service + Controller | state |
```

2. **Present the list** to the user if the spec is large or ambiguous.
3. **Implement tests** in `backend/src/test/java/` mirroring main package structure.
4. **Run tests** and fix failures before finishing.

## Output summary

When done, report:

```
## Tests generated
- ServiceTest: N tests — <brief list of behaviours>
- ControllerTest: N tests — ...
- RepositoryTest: N tests — ...

## Spec gaps (if any)
- Behaviour implied but not in spec — needs clarification before testing

## Not covered (intentionally)
- What was skipped and why
```

## Rules

- Prefer **AssertJ** and **MockMvc** per `rules/testing.mdc`.
- Use H2 + `test` profile; Testcontainers only when spec requires Postgres-specific behaviour.
- Match existing test style in the project if tests already exist.
- If implementation is missing, generate test skeletons with `@Disabled("awaiting implementation")` only when user explicitly requests TDD-style stubs.
