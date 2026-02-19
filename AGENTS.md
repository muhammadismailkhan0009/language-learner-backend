# AGENTS.md

Agents must follow these guideline files:

- `codebase-architecture-guidelines/backend/Architecture-Patterns.md`
- `codebase-architecture-guidelines/backend/Dependency-Boundaries.md`
- `codebase-architecture-guidelines/backend/Module-Boundaries.md`
- `codebase-architecture-guidelines/backend/Test_Backend.md`
- `codebase-architecture-guidelines/backend/Refactoring.md`
- `codebase-architecture-guidelines/backend/Backend.md`

## Boundary Mapping Rule

- Use MapStruct for mappings at architecture boundaries (API <-> application/domain contracts, and domain <-> persistence entities).
- Keep boundary mappings explicit and centralized in mapper classes/interfaces.
