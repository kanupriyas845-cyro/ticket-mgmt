# Requirements — TicketMgmtSystem

**Status:** Draft  
**Last updated:** 2026-09-25  
**Source:** User-provided instructions from project kickoff (Sep 2026)

This document captures what was **explicitly requested**. Items not stated by the user are listed under **Assumptions** or **Open questions** — not silently decided.

---

## 1. Project goal

Build a **support ticket management system** — a web application for creating and managing support tickets.

> The functional details below are minimal. Only capabilities directly implied by “ticket management” or explicitly requested are listed as requirements. Everything else is an assumption or open question.

---

## 2. Explicit requirements

### 2.1 Technical stack

These were stated directly by the user:

| ID | Requirement |
|----|-------------|
| **TR-01** | Backend uses **Java 21**. |
| **TR-02** | Backend uses **Spring Boot**. |
| **TR-03** | Database is **PostgreSQL** (production) and/or **H2** (local development / testing). |
| **TR-04** | Frontend uses **React** and **Next.js**. |
| **TR-05** | Frontend tooling uses **Node.js** and **npm**. |
| **TR-06** | Build tool is **Gradle** or **Maven**. |
| **TR-07** | Use **stable, compatible** versions — not “latest for the sake of latest”. Pinned versions live in `docs/project-versions.md`. |

### 2.2 Architecture (implied by stack)

| ID | Requirement |
|----|-------------|
| **TR-08** | The system is a **web application**: a Spring Boot backend API and a Next.js frontend. |
| **TR-09** | Backend and frontend are separate deployable parts (not a monolithic Thymeleaf app). |

### 2.3 Process

| ID | Requirement |
|----|-------------|
| **PR-01** | Development is **spec-driven**: requirements and feature specs are written before implementation. |
| **PR-02** | Project instructions (rules, skills, commands) are maintained for repeatable AI-assisted development. |

### 2.4 Core functional (inferred from project purpose)

The user named the project **TicketMgmtSystem** and referred to a **ticket management system**. The following are the minimum capabilities implied by that purpose — no extra features added:

| ID | Requirement |
|----|-------------|
| **FR-01** | A user can **create** a support ticket. |
| **FR-02** | A user can **view** a ticket's details. |
| **FR-03** | A user can **list** tickets. |
| **FR-04** | A user can **update** a ticket (e.g. title, description, or other core fields). |

> **Note:** The user did not specify ticket fields, statuses, roles, or workflows. Those are **not** requirements until confirmed (see §4 and §5).

---

## 3. Out of scope (not requested)

The following were **not** stated by the user and must **not** be implemented unless added to this document:

- User authentication / authorisation
- User roles (requester, agent, admin)
- Assigning tickets to agents
- Comment threads on tickets
- Ticket status workflow / state machine
- Search and filter
- Dashboard or metrics
- Categories, priorities, or tags
- Email notifications
- SLA / escalation rules
- Mobile native apps
- Multi-tenancy

> Some of these appear in `spec/00-product-overview.md` as *planned* ideas from an early draft. That file is **not** authoritative until requirements here are updated and approved.

---

## 4. Assumptions

Used only where the requirement is ambiguous and work cannot proceed without a temporary default. **Replace with confirmed decisions.**

| ID | Assumption | Rationale |
|----|------------|-----------|
| **A-01** | A ticket has at minimum: **title**, **description**, and **created timestamp**. | Minimum viable ticket entity; user did not define fields. |
| **A-02** | REST API on backend, UI on frontend. | Standard split for Spring Boot + Next.js; not explicitly stated but follows from TR-08/09. |
| **A-03** | **Gradle wrapper** is the preferred build tool over global Maven. | Documented in `docs/project-versions.md`; user allowed either. |
| **A-04** | **H2** for local/test profiles; **PostgreSQL** for dev/staging/prod-like environments. | User said “PostgreSQL or H2”; both are used in different contexts. |
| **A-05** | Single-tenant deployment (one organisation). | User did not mention multi-tenancy. |
| **A-06** | English-only UI and API messages. | User did not mention i18n. |

---

## 5. Open questions

Resolve these with the user before implementing related features:

| ID | Question | Blocks |
|----|----------|--------|
| **Q-01** | What **fields** does a ticket have beyond title/description (priority, category, requester)? | Data model, API, UI forms |
| **Q-02** | Is **authentication** required? Local accounts, OAuth, or none for v1? | Security, all protected endpoints |
| **Q-03** | Are there **user roles** and what can each role do? | Authorisation |
| **Q-04** | What **ticket statuses** exist and which transitions are allowed? | State machine, API, tests |
| **Q-05** | Can tickets be **assigned** to specific users/agents? | Assignment feature |
| **Q-06** | Are **comments** required on tickets? | Comments API and UI |
| **Q-07** | Is **search/filter** required in v1? | List UI and query API |
| **Q-08** | Gradle or Maven — final choice? | Project scaffold (A-03 assumes Gradle) |

---

## 6. Non-functional requirements (minimal)

Derived from explicit technical and process requirements only:

| ID | Requirement |
|----|-------------|
| **NFR-01** | Versions must match `docs/project-versions.md`. |
| **NFR-02** | API follows conventions in `spec/02-conventions.md` and `rules/api-standards.mdc`. |
| **NFR-03** | Backend code follows `rules/java-springboot.mdc`. |
| **NFR-04** | Tests follow `rules/testing.mdc`; behaviour driven by specs, not coverage targets. |
| **NFR-05** | No secrets in source control. |

---

## 7. Requirement traceability

| User input (paraphrased) | Captured as |
|--------------------------|-------------|
| “Java 21” | TR-01 |
| “Spring boot” | TR-02 |
| “PostgreSQL or H2” | TR-03, A-04 |
| “React/Next js” | TR-04 |
| “Node/npm” | TR-05 |
| “Gradle or Maven” | TR-06, A-03, Q-08 |
| “Stable compatible versions” | TR-07, NFR-01 |
| “Ticket management system” (project name / purpose) | §1, FR-01–FR-04 |
| “Spec-driven structure” | PR-01, PR-02 |
| “Don’t build until asked” | Process — implementation gated on user approval |

---

## 8. Next steps

1. User confirms or updates **§4 Assumptions** and answers **§5 Open questions**.
2. Move confirmed functional scope into feature specs under `spec/features/`.
3. Update `spec/00-product-overview.md` to align with this document (remove unconfirmed “planned” features).
4. Scaffold backend and frontend after requirements are approved.
