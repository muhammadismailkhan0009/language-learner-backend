# Module Boundaries
(Responsibility & Size Rules for Backend Systems)

## Purpose

This document complements `Backend.md` by defining practical module boundaries.
It keeps backend modules understandable, testable, and safe to change as systems grow.

---

## Core Rule

> One module should have one primary reason to change.

A module can coordinate multiple calls, but must not mix unrelated concerns.

Baseline requirement:
- Module decomposition must preserve capability ownership and Ports/Adapters separation.
- Module decomposition must maximize locality of behavior and minimize cognitive load.

---

## Responsibility Boundaries by Area

### `api/`
- Transport-facing request/response handling.
- Input validation, translation, and protocol concerns.
- No core domain policy decisions.
- Acts as a primary/driving adapter into application use-cases.

### `contracts/`
- Stable boundary contracts for API, events, and module integration.
- Defines shape/meaning expectations at boundaries.
- Must avoid embedding orchestration or domain decision logic.

### `application/`
- Use-case orchestration and policy coordination.
- Coordinates domain operations and integration through ports.
- Must not depend on concrete infrastructure adapters.
- Must not own core domain invariants or domain state-transition rules.

### `domain/`
- Domain rules, invariants, and core business behavior.
- Independent from transport and infrastructure concerns.
- Must not depend on runtime/framework APIs.
- Owns validation and state transition logic for domain models.

### `infrastructure/`
- Runtime/integration adapters for persistence, networking, queues, files, etc.
- Maps external protocols into internal contracts.
- No domain policy ownership.
- Acts as secondary/driven adapters implementing declared ports.

### `shared/`
- Cross-module stable primitives and contracts.
- No capability-specific business behavior.
- Must remain small and low-churn.

### `bootstrap/`
- Startup wiring and composition only.
- No module policy logic.

---

## Onion Integrity Checks

Required:
- Inner layers (`domain`, `application`) must never depend on outer adapters.
- Outer layers (`api`, `infrastructure`) may depend inward only.
- Any outward dependency from `domain` is an architecture violation.

---

## File Size and Complexity Guardrails

- Target file size: `<= 150` lines.
- Review threshold: `> 200` lines should be split unless justified.
- Hard threshold: `> 300` lines is a required split.

Split triggers:
- Mixes transport + orchestration + infrastructure concerns.
- Requires unrelated section headers to explain one file.
- Requires unrelated tests for one file to be understandable.

---

## State Ownership

- Domain state and invariants live in `domain/*`.
- Use-case operation state lives in `application/*`.
- Runtime integration state belongs in `infrastructure/*`.
- Shared modules should be stateless or explicitly stateful with narrow purpose.

---

## Side-Effect Boundaries

Allowed side effects:
- `bootstrap/*`, `api/*`, `infrastructure/*`, selected `application/*` orchestration

Prefer side-effect free:
- `domain/*`, `shared/*`, most policy logic in `application/*`

---

## Naming Conventions

Use names that reflect intent:
- `handleX`, `createX`, `updateX`, `deleteX` for application use-cases
- `toXDto`, `fromXRequest` for transport mapping
- `XRepository`, `XClient`, `XAdapter` for infrastructure adapters

Avoid generic names such as `helpers`, `utils`, `manager`
unless folder-scoped and purpose-scoped.

---

## Boundary Violations (Examples)

- Domain entities directly calling ORM/repository implementations.
- Application services importing concrete DB/network SDK clients.
- API handlers containing full domain policy decision trees.
- Shared modules carrying module-specific business policy.

When violated, split by axis:
1. transport (`api`)
2. orchestration (`application`)
3. domain policy (`domain`)
4. integration adapter (`infrastructure`)
5. reusable primitive/contract (`shared`)
