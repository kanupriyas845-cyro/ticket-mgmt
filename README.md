# TicketMgmtSystem

A ticket management system (greenfield — not yet scaffolded).

## Spec-Driven Structure

```
TicketMgmtSystem/
├── AGENTS.md           # Entry point for AI agents
├── rules/              # Cursor rules (.mdc)
├── skills/             # Agent skills (workflows)
├── commands/           # Slash commands
├── spec/               # Product & feature specifications
├── docs/               # Project documentation
└── .specstory/history/ # Session history (SpecStory)
```

Cursor loads `rules/`, `skills/`, and `commands/` via symlinks in `.cursor/`.

## Getting Started

1. Read `docs/project-versions.md` for toolchain versions.
2. Read `spec/00-product-overview.md` for product scope.
3. Use `AGENTS.md` when working with AI agents.

## Status

- [x] Version matrix defined
- [x] Spec-driven project structure
- [ ] Backend scaffold (Spring Boot)
- [ ] Frontend scaffold (Next.js)
- [ ] Feature specs
