# Backend System Refactoring Instructions (LLM-Guided)
(Safety Rules for Behavior-Preserving Change)

## Purpose

This document defines **how refactoring is performed, constrained, and validated**
in this backend system codebase when using AI-assisted development.

These rules are **normative** and reflect the author's quality goals:
- bug resistance
- maintainability
- safe long-term evolution

This file exists to:
- preserve behavior while improving structure
- reduce accidental regressions across module boundaries
- keep LLM-assisted refactors predictable and auditable
- reduce cognitive load and improve long-term maintainability

---

## Scope Boundary

This document:
- defines **refactoring intent and safety process**
- applies only to **backend system code**
- governs **how LLMs may refactor code**

This document does **not**:
- authorize feature work
- redefine business behavior
- replace architecture or testing guides

---

## Core Invariant

> **Refactoring changes structure, not behavior.**

Behavior changes are feature changes and require explicit human instruction.

---

## What Counts as Refactoring

Allowed refactoring categories:
- rename for clarity
- extract function/module for cohesion
- split oversized files by responsibility
- move code to correct module boundary
- replace duplicated logic with shared abstraction
- remove dead code that is provably unused

Not refactoring:
- adding new behaviors
- changing externally observable outcomes
- modifying module/API/event/persistence contracts
- changing error policy semantics

---

## LLM Refactoring Rules

### What LLMs MAY Do Automatically

- reorganize code to match architecture boundaries
- improve naming and file placement
- isolate side effects behind ports/adapters
- reduce coupling and cyclic dependencies
- simplify control flow without semantic change

### What LLMs MUST NOT Do Automatically

- change externally observable behavior
- alter assertions in existing tests to "fit" refactored code
- silently change contract shapes
- silently change persistence semantics
- remove fallback/error paths without explicit approval

---

## Refactoring Triggers

Refactor is recommended when one or more apply:
- file exceeds maintainability threshold defined in `Module-Boundaries.md`
- one module has multiple unrelated reasons to change
- domain/application code directly depends on runtime/infrastructure APIs
- infrastructure concerns leak into domain/application modules
- repeated logic appears across modules
- dependency direction violates `Dependency-Boundaries.md`

---

## Required Refactoring Workflow

1. Identify invariant behavior to preserve.
2. Identify target boundary and intended module responsibilities.
3. Refactor in small, reviewable steps.
4. Run relevant tests after each step.
5. Confirm no contract drift (API, domain, persistence, events).
6. Stop and escalate if behavior-preserving change is impossible.

No large "all-at-once" refactors without explicit human request.

---

## Backend-Specific Safety Checks

For each refactor, verify:
- module ownership and boundaries remain explicit
- domain/application remain independent from concrete infrastructure adapters
- dependency direction still points inward to the domain core
- primary (`api`) and secondary (`infrastructure`) adapter roles remain explicit
- persistence mapping semantics remain stable
- API contract behavior and error semantics remain stable
- transactional boundaries and idempotency behavior remain stable

---

## Contract Preservation Rules

Treat the following as contract boundaries:
- API request/response and status/error semantics
- event/message payload and ordering semantics
- persistence schema/field meaning and migration behavior
- domain invariant outcomes

Changing any contract boundary is not refactoring unless explicitly approved.

---

## Anti-Patterns

Reject these changes as unsafe refactoring:
- "cleanup" that also changes behavior
- moving code across boundaries without updating dependency direction
- placing infrastructure calls directly inside domain logic
- extracting generic helpers that hide domain meaning
- deleting tests because refactor "made them hard to maintain"

---

## Completion Criteria

A refactor is complete only when:
- architectural boundaries are improved or preserved
- tests pass without weakening assertions
- no contract boundary changed unintentionally
- code is easier to navigate and reason about than before

---

## Final Invariant

> **LLMs may optimize structure.**
> **They may not reinterpret intent.**
> **They may not weaken guarantees.**

All refactoring automation must preserve this invariant.
