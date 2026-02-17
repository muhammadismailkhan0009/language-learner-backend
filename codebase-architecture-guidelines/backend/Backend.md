# Backend System Architecture Guidelines
(Onion Architecture, Modular Monolith)

## Purpose

This document defines the **structural organization** of backend systems.
These guidelines are **normative rules** for this codebase and reflect the author's coding style.

Its goal is to:
- make navigation predictable
- preserve strong module boundaries
- keep business behavior isolated from runtime/integration details
- reduce cognitive load and long-term maintenance cost

It **does not**:
- by itself mandate implementation tactics
- force communication style choices
- prescribe persistence technology
- define testing or refactoring workflow

Scope boundary:
- this file defines **directory structure and placement intent only**
- it does not define dependency rules, behavior rules, or implementation tactics

---

## Core Principle

> **Backend modules are capability boundaries; structure must preserve behavioral locality.**
> **Dependency direction points inward toward the domain core.**

A module owns its behavior, contracts, and policies.
Shared areas exist to reduce duplication, not to centralize module-specific behavior.

---

## Architectural Shape

- Primary shape: **modular monolith**
  - one deployable unit
  - internally divided into bounded modules
- Internal architecture style: **Onion Architecture**
  - `domain` is the core
  - `application` orchestrates use-cases around the core
  - `api` and `infrastructure` are outer adapters
  - dependencies must point inward
- Modules represent **business capabilities**, not technical layers.
- DDD-aligned boundaries are required across backend modules.
- For simple capabilities, use lightweight DDD boundaries rather than no boundaries.

---

## Top-Level Structure

src/
├── modules/
├── shared/
└── bootstrap/


---

## Directory Intent

### `modules/`
- Primary capability boundaries.
- Each module owns its behavior, contracts, and integration policy.
- New functionality should prefer existing modules before creating new ones.

Example:

modules/
├── billing/
├── user-management/
└── reporting/


### `shared/`
- Cross-module reusable modules not owned by one capability.
- Must remain small, stable, and behavior-agnostic.
- Must not absorb module-specific business policy.

Examples:
- shared contracts and primitives
- cross-module technical abstractions
- stable shared-kernel types (when justified)

### `bootstrap/`
- Application startup, wiring, and runtime composition.
- Dependency assembly and infrastructure initialization.
- No domain policy or use-case behavior.

---

## Inside a Module

Each module organizes around **domain and use-case behavior**, not generic layer dumping.

Typical internal structure (illustrative):

modules/order/
├── domain/
├── application/
├── infrastructure/
└── api/


Notes:
- Use `kebab-case` for module directory names.
- Module boundaries must be visible in folder names and imports.
- Internal structure may vary only when boundary clarity is improved.

---

## Sub-Contexts Within a Module

Some modules may define sub-contexts for additional boundary clarity.
When sub-contexts exist, they become the primary placement target.

Example:

modules/language-learning-system/
├── reading-and-listening/
├── writing/
└── vocabulary/


Rules:
- Place new/moved behavior inside the correct sub-context when one exists.
- Avoid placing mixed sub-context behavior directly at module root.
- When relocating behavior, move the full vertical slice (api/application/domain/infrastructure/tests).

---

## Growth Rules

- Add functionality inside existing module when behavior belongs there.
- Create a new module only for a genuinely new capability boundary.
- Module boundaries may evolve, but dependency direction and ownership must stay explicit.

---

## Non-Goals

This document intentionally avoids:
- prescribing framework-specific implementation style
- choosing persistence/eventing strategy
- defining testing/refactoring policy

Its purpose is **structural clarity only**.

---

## Companion Documents

For full governance, the following companion guides are mandatory.

Path note:
- In this source repo, use `backend/...`.
- In copied package output, use `codebase-architecture-guidelines/backend/...`.

Companion files:
- `backend/Module-Boundaries.md` or `codebase-architecture-guidelines/backend/Module-Boundaries.md`
- `backend/Dependency-Boundaries.md` or `codebase-architecture-guidelines/backend/Dependency-Boundaries.md`
- `backend/Architecture-Patterns.md` or `codebase-architecture-guidelines/backend/Architecture-Patterns.md`
- `backend/Test_Backend.md` or `codebase-architecture-guidelines/backend/Test_Backend.md`
- `backend/Refactoring.md` or `codebase-architecture-guidelines/backend/Refactoring.md`

---

## Precedence

When rules overlap, apply documents in this order:
1. `backend/Architecture-Patterns.md` (or copied path equivalent)
2. `backend/Dependency-Boundaries.md` (or copied path equivalent)
3. `backend/Module-Boundaries.md` (or copied path equivalent)
4. `backend/Test_Backend.md` (or copied path equivalent)
5. `backend/Refactoring.md` (or copied path equivalent)
6. `backend/Backend.md` (or copied path equivalent)

Interpretation rule:
- `Backend.md` governs structure and placement.
- Companion documents govern architecture constraints, dependency direction,
  testing policy, and safe refactoring behavior.
