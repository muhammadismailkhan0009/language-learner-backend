# Architecture Patterns for Backend Systems
(Onion Architecture, DDD Alignment, Ports/Adapters, Optional CQRS)

## Purpose

This document defines mandatory and optional architecture patterns for backend system code.
It is normative for this codebase and intended to keep backend systems extensible,
testable, and maintainable.

---

## Normative Baseline

> Onion dependency rule, DDD-aligned capability boundaries, and Ports/Adapters are mandatory.

Rules:
- Business behavior must be organized by module capability boundaries.
- Dependency direction must point inward to the domain core.
- Use-case orchestration must be separated from runtime/integration adapters.
- Ports/Adapters (interface-based boundaries) are required for external integrations.
- DDD-aligned boundaries are mandatory for backend modules.
- CQRS is optional and should be used only when read/write concerns materially diverge.

---

## Onion Dependency Rule (Mandatory)

Required:
- `domain` is the innermost layer and must not depend on outer layers.
- `application` may depend on `domain`, never on concrete `api`/`infrastructure`.
- `api` and `infrastructure` are outer adapters that depend inward.
- Runtime/framework concerns stay outside `domain`.

Disallowed:
- `domain -> application|api|infrastructure`
- `application -> api|infrastructure` (except via ports/contracts)
- any outer-layer policy leaking into domain invariants

---

## Capability Boundary Requirement

Required usage:
- Use capability-aligned names for modules, use-cases, and contracts.
- Keep module behavior local; avoid cross-module behavior leakage.
- Keep domain policy separate from transport and persistence adapter code.

Recommended placement:
- `modules/<capability>/domain/*`
- `modules/<capability>/application/*`
- `modules/<capability>/contracts/*`
- shared cross-module contracts only in `shared/*` when ownership is truly shared.

---

## Boundary Translation Policy

Required:
- Boundary-shape translation must happen at architecture boundaries.
- Transport contracts (request/response/message DTOs) must not leak into domain models.
- Use-case orchestration may coordinate translation, but domain rules must not depend on transport contract types.

Recommended:
- Keep translation mechanisms centralized and consistent per boundary (e.g., dedicated mapper modules/functions).
- Prefer explicit mapping over implicit field sharing when crossing boundaries.

---

## DDD Alignment Policy

Required for backend modules:
- explicit domain language and domain boundaries
- clear distinction between domain rules and infrastructure concerns

For simple capabilities:
- keep DDD boundaries lightweight, but still explicit
- do not collapse domain/application/infrastructure into one mixed module

---

## Pattern Usage Policy

All software design-pattern families are allowed when context justifies them, including:
- creational patterns
- structural patterns
- behavioral patterns
- architectural patterns beyond DDD/CQRS

Pattern usage rules:
- No pattern is mandatory by default except DDD boundaries and Ports/Adapters.
- Pattern selection must be context-driven (requirements, complexity, change risk).
- Do not introduce a pattern if it increases abstraction cost without reducing risk.

Do not force tactical DDD ceremony or generic pattern ceremony for simple capabilities.

---

## Complexity Control Policy

Required:
- Prefer the smallest design that satisfies current behavior and boundary constraints.
- Introduce new abstraction layers only when they reduce concrete change risk.

Review triggers:
- adding intermediate models/types that mirror existing domain contracts without clear boundary value
- creating orchestration/policy duplication across layers
- introducing framework-driven patterns that increase abstraction cost without ownership clarity

---

## Ports and Adapters Requirement

Required usage:
- Define ports from application/domain needs, not from tool/vendor APIs.
- Implement adapters in infrastructure/runtime-facing modules.
- Wire concrete adapters in `bootstrap/*`.

Required structure:
- Port interfaces: `modules/<capability>/application/ports/*` or `shared/ports/*`
- Adapter implementations: `modules/<capability>/infrastructure/*`
- Domain/application logic depends on ports, not concrete adapters.

Adapter role model:
- Primary/driving adapters: `api/*` entrypoints invoking application use-cases.
- Secondary/driven adapters: `infrastructure/*` implementations used through ports.

Integration points that must use ports:
- persistence/database clients
- external APIs and message brokers
- file/object storage
- telemetry and observability sinks

---

## CQRS Policy (Optional)

CQRS is optional by default.

Adopt CQRS when:
- command and query concerns have materially different complexity
- write side-effects need distinct handling from read paths
- separation reduces coupling and improves maintainability

Suggested structure when used:
- `modules/<capability>/application/commands/*`
- `modules/<capability>/application/queries/*`
- `modules/<capability>/contracts/*`

Do not adopt CQRS for style consistency alone.

---

## Enforcement

Code review requirements:
- Every module must preserve capability and ownership boundaries.
- External/runtime integrations must pass through declared ports/adapters.
- Any direct concrete integration inside domain/application code is a violation.
- CQRS is required only when divergence criteria are met.

Merge/CI gates (objective):
- Import-boundary checks must pass for `modules`, `shared`, and `bootstrap`.
- Any direct infrastructure/runtime API usage in domain/application code fails review unless explicitly approved.
- Any direct concrete adapter import in domain/application code fails review unless explicitly approved.
- New module changes must declare owning capability and boundary intent in PR notes.

---

## Non-Goals

This guide does not mandate:
- framework/vendor choices
- persistence technology choices
- unnecessary tactical-pattern overhead

Primary goal: stable boundaries with pragmatic complexity control.
