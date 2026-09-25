# Project Version Matrix

**Document date:** 2026-09-24  
**Project:** TicketMgmtSystem  
**Status:** Greenfield (no build tool, backend framework, or frontend scaffolded yet)

This document records the **stable, compatible** versions selected for this project. Versions were chosen for long-term support, mutual compatibility, and suitability for a new production-style ticket management system — not simply because they are the newest releases.

---

## Selected Versions

| Layer | Component | Version | Notes |
|-------|-----------|---------|-------|
| **Runtime** | Java (OpenJDK) | **21.0.x LTS** | Required project baseline; supported until Sep 2031 |
| **Backend** | Spring Boot | **4.1.1** | Current OSS-supported Spring Boot line (Jul 2027) |
| **Backend** | Spring Framework | **7.0.x** | Managed by Spring Boot 4.1 BOM |
| **Build** | Gradle (wrapper) | **8.14.3** | Minimum for Spring Boot 4.1; no global install required |
| **Build** | Maven (alternative) | **3.9.9** | Only if the team prefers Maven over Gradle |
| **Database (prod)** | PostgreSQL | **17.x** (latest patch, e.g. 17.9+) | Production / staging target |
| **Database (local)** | H2 | **2.3.x** | Managed by Spring Boot BOM; fast local dev & tests |
| **Frontend** | Next.js | **15.5.21** | Maintenance LTS; security-patched, less churn than 16.x |
| **Frontend** | React | **19.1.x** | Stable pairing with Next.js 15 |
| **Frontend** | TypeScript | **5.7.x** | Meets Next.js 15 minimum (5.1+) with mature tooling |
| **Tooling** | Node.js | **20.20.0 LTS** (Iron) | Active LTS; compatible with Next.js 15 |
| **Tooling** | npm | **10.9.x** | Ships with Node 20 LTS |

---

## Version Selection Rationale

### Java 21

- Explicit project requirement.
- Long-term support release (LTS) with broad library and framework support.
- Already installed on the development machine (`21.0.12`), but not currently the shell default.

### Spring Boot 4.1.1

- As of September 2026, Spring Boot **3.5.x and 3.4.x have reached end of open-source support**.
- Spring Boot **4.1.1** is the recommended line for **new** projects: OSS support until **July 2027**, Java 17–26 compatibility, and current security patches.
- Spring Boot **4.0.x** is still supported but reaches OSS EOL in **December 2026**; 4.1.x has a longer runway.
- Fully compatible with **Java 21** without workarounds.

> **Why not Spring Boot 3.5.16?** It is the most mature 3.x release and works well with Java 21, but its OSS support ended in June 2026. For a greenfield app, starting on an unsupported line is not recommended.

### Gradle 8.14.3 (with wrapper) over global Maven

- Spring Boot 4.1 requires **Gradle 8.14+** or **Maven 3.6.3+**.
- **Gradle wrapper** (`gradlew`) is recommended because:
  - No global Gradle install is needed (none is currently on the machine).
  - The project pins an exact build-tool version for all developers and CI.
  - Gradle is the default choice on [start.spring.io](https://start.spring.io) and common in Spring Boot tutorials.
- **Maven 3.9.9** is documented as an alternative if the team has a strong Maven preference.

### PostgreSQL 17.x (production) + H2 (local)

- **PostgreSQL 17** is a stable major release with community support until approximately **November 2029** — the longest support window among practical choices for a new service in 2026.
- **H2** is ideal for:
  - Fast local development without running a database server.
  - Integration tests in CI.
  - Spring Boot provides first-class H2 support via `spring-boot-starter-data-jpa` and profile-based configuration.
- Recommended approach: **H2 for `local`/`test` profiles**, **PostgreSQL for `dev`/`staging`/`prod`**.

> **Why not PostgreSQL 16?** Still supported and perfectly viable, but 17 offers a longer support horizon for a new project with minimal migration risk.

### Next.js 15.5.21 + React 19.1.x

- **Next.js 15.5.x** is on **Maintenance LTS** (released October 2024) and continues to receive critical fixes and security patches.
- **Next.js 16.x** is Active LTS but introduces more breaking changes (async request APIs, Node 20.9+ requirement, Turbopack defaults). For a first build, 15.5.x is the more conservative stable choice.
- **React 19** is the stable React major version paired with Next.js 15.
- The App Router is recommended for a new project.

> **Upgrade path:** Move to Next.js 16.x + Node 22 LTS once the core app is stable and the team is ready for the migration.

### Node.js 20.20.0 LTS + npm 10.9.x

- **Node 20 (Iron)** is Active LTS and meets Next.js 15 requirements (18.18+).
- Chosen over Node 22 to stay on the more conservative LTS line while building the initial frontend.
- Chosen over Node 18 because Node 18 is near end-of-life and Next.js 16 (future upgrade) drops Node 18 support.
- **npm** version should match the Node 20 installation (typically 10.9.x); avoid mixing Cursor's bundled Node with nvm's Node in the same project.

### TypeScript 5.7.x

- Next.js 15 requires TypeScript 5.1+.
- 5.7.x is a mature release with stable editor and tooling support without adopting pre-release features.

---

## Compatibility Matrix (at a glance)

```
Java 21
  └── Spring Boot 4.1.1
        ├── Gradle 8.14.3 (wrapper)
        ├── PostgreSQL 17.x  (prod/staging)
        └── H2 2.3.x         (local/test)

Node 20.20 LTS
  └── npm 10.9.x
        └── Next.js 15.5.21
              └── React 19.1.x + TypeScript 5.7.x
```

---

## Local Machine Audit (2026-09-24)

### Already installed

| Tool | Detected Version | Status |
|------|------------------|--------|
| OpenJDK 11 | 11.x (`/usr/lib/jvm/java-11-openjdk-amd64`) | Installed, not needed for this project |
| OpenJDK 17 | **17.0.20** (current shell default) | Installed, wrong default for this project |
| OpenJDK 21 | **21.0.12** (`/usr/lib/jvm/java-21-openjdk-amd64`) | Installed, **use this** |
| Node.js (Cursor bundled) | **v22.22.0** (`/usr/share/cursor/.../node`) | Available in PATH, not the project target |
| Node.js (nvm) | **v18.19.0** (nvm default) | Installed, below project target |
| npm | **10.2.3** (via nvm Node 18) | Installed |
| Docker CLI | **29.1.3** | Installed (daemon was not running during audit) |

### Not installed (or not on PATH)

| Tool | Status |
|------|--------|
| Gradle | Not installed globally — **use Gradle wrapper instead** |
| Maven | Not installed |
| PostgreSQL (`psql`) | Not installed |
| Java 24 | Referenced in IntelliJ project settings but **not installed** on the system |

### IDE configuration mismatch

IntelliJ `.idea/misc.xml` is set to **JDK 24 / openjdk-24**, which is not installed. When scaffolding the project, reconfigure the IDE module SDK to **Java 21**.

---

## Setup Actions Before Building

These are preparatory steps only — no project code has been generated yet.

### 1. Set Java 21 as default for this project

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
export PATH="$JAVA_HOME/bin:$PATH"
java -version   # should report 21.0.x
```

Add the `JAVA_HOME` export to `~/.bashrc` or use a per-project `.envrc` / IDE SDK setting.

### 2. Install and pin Node 20 via nvm

```bash
nvm install 20.20.0
nvm use 20.20.0
nvm alias default 20.20.0
node -v   # should report v20.20.0
npm -v    # should report 10.9.x
```

Add a `.nvmrc` file containing `20.20.0` in the frontend directory when it is created.

### 3. PostgreSQL for non-local environments

Choose one:

- **Docker** (recommended for local dev parity):
  ```bash
  docker run -d --name ticketmgmt-pg \
    -e POSTGRES_DB=ticketmgmt \
    -e POSTGRES_USER=ticketmgmt \
    -e POSTGRES_PASSWORD=<set-a-strong-password> \
    -p 5432:5432 \
    postgres:17
  ```
- **Native install:** `sudo apt install postgresql-17 postgresql-client-17`

### 4. No global Gradle/Maven install needed

When the backend is scaffolded, commit the **Gradle wrapper** (`gradlew`, `gradle/wrapper/`) so builds work without a global Gradle install.

---

## Alternatives Considered

| Choice | Alternative | Why not selected (for now) |
|--------|-------------|---------------------------|
| Spring Boot 4.1.1 | 3.5.16 | OSS support ended June 2026 |
| Spring Boot 4.1.1 | 4.0.8 | Shorter OSS support window (ends Dec 2026) |
| Gradle wrapper | Global Maven 3.9.9 | Maven not installed; Gradle wrapper avoids tooling gaps |
| PostgreSQL 17 | PostgreSQL 16 | Shorter community support window |
| PostgreSQL 17 | H2 only | Not suitable for production-like staging/prod |
| Next.js 15.5.21 | Next.js 16.2.x | More breaking changes; better as a later upgrade |
| Node 20 LTS | Node 22 LTS | Node 22 is valid but 20 is the more conservative LTS pairing with Next 15 |
| Node 20 LTS | Node 18 (current nvm default) | Node 18 is aging; blocks a clean upgrade to Next 16 |

---

## References

- [Spring Boot 4.1 System Requirements](https://docs.spring.io/spring-boot/system-requirements.html)
- [Spring Boot release support (endoflife.date)](https://endoflife.date/spring-boot)
- [Next.js Support Policy](https://nextjs.org/support-policy)
- [PostgreSQL Versioning Policy](https://www.postgresql.org/support/versioning/)
- [Node.js Release Schedule](https://github.com/nodejs/release#release-schedule)
