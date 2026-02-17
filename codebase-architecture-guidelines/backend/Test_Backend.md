# Backend System Test Instructions (LLM-Guided)
(Behavioral Testing & Safe Automation Rules)

## Purpose

This document defines **how tests are created, evolved, and maintained**
in this backend system codebase when using AI-assisted development.

These rules are **normative** and reflect the author's testing philosophy.

This file exists to:
- preserve behavioral intent
- keep tests stable under refactoring
- prevent silent semantic drift
- make LLM-assisted coding predictable and safe

---

## Scope Boundary

This document:
- defines **testing intent, semantics, and automation rules**
- applies only to **backend system tests**
- governs **how LLMs may interact with tests**

This document does **not**:
- define architecture or directory structure
- mandate testing frameworks or tools
- require TDD
- define coverage targets
- prescribe mocking libraries or styles

---

## Backend Test Taxonomy

Use these categories as behavioral lenses:
- **Unit tests**: one declared behavioral guarantee.
- **Integration tests**: composition of multiple guarantees.
- **Contract tests**: API/event/storage contract stability.
- **End-to-end/system tests**: critical backend journeys through real composition.
- **Non-functional tests**: performance, reliability, and security guarantees.

Tests are selected by risk and declared behavior, not by tooling style.

---

## Dual-Axis Test Model

Testing decisions use two independent axes:
- **Behavioral scope**: what guarantee is asserted (unit, integration, contract, end-to-end, non-functional).
- **Isolation scope**: how much runtime composition is required to validate that guarantee.

These axes are orthogonal:
- a behaviorally small test may run with broad runtime composition
- a behaviorally small test may also be fully isolated

LLMs must preserve both axes intentionally. Neither axis may be inferred from naming alone.

---

## Isolation Scope Rules (Framework-Agnostic)

### Pure Logic Scope

Use pure tests (no application/runtime context) for deterministic logic guarantees such as:
- domain invariants
- validation rules
- pure transformations and conversions
- static utilities
- mappers and format translators
- calculators/parsers without runtime dependencies

These tests must be fast, deterministic, and independent of infrastructure semantics.

### Boundary-Isolated Scope

Use boundary-isolated tests when behavior depends on interaction with collaborators, while still avoiding full composition.

### Composed Runtime Scope

Use composed-runtime tests when behavior depends on real composition semantics (wiring, persistence semantics, transport semantics, lifecycle, transactions, or equivalent runtime behavior).

---

## Core Principles

### 1. Tests Encode Intent, Not Structure

- Tests represent **human-declared behavioral guarantees**
- Tests are **commitments**, not experiments
- Tests must not be inferred from code structure alone

LLMs must never invent, assume, or widen behavior.

---

### 2. Definition of a Unit Test

A **unit test** is defined by **behavioral scope**, not by technology.

A unit test:
- asserts **one externally meaningful behavioral guarantee**
- may execute with:
  - full application runtime context
  - real persistence mechanisms
  - real infrastructure components
- is still a unit test if it verifies **one behavior**

Unit tests are **behavioral**, not structural.

---

### 3. Definition of an Integration Test

An **integration test** verifies **composition of multiple behaviors**.

Integration tests:
- assert interaction between independent guarantees
- are **explicit and intentional**
- must never be inferred automatically

Use of real infrastructure or full runtime context alone does not imply integration testing.

---

### 4. Definition of a Contract Test

A **contract test** verifies boundary stability for API, message, and persistence semantics.

Contract tests:
- assert shape and semantics of boundary contracts
- guard compatibility during refactors and releases
- fail when boundary behavior drifts unexpectedly

---

### 5. Definition of an End-to-End/System Test

An **end-to-end/system test** verifies a declared critical backend journey
through full application composition.

End-to-end/system tests:
- verify business-critical outcomes across module and integration boundaries
- are intentionally focused and limited in number
- must be deterministic and environment-stable

End-to-end tests do not replace unit/integration/contract tests.

### 6. Replacement Constraint

Composed-runtime and end-to-end tests do **not** replace pure-logic tests for declared deterministic guarantees.

If a deterministic rule exists, it must have at least one direct pure test at the rule boundary.

### 7. Location and Naming Integrity

Test location and naming must not claim stronger isolation than the test actually uses.

If a test uses composed runtime, it must be identified as composition/flow/integration in naming and placement conventions used by the repository.

---

## Behavior Declaration Rules

- Behavior is **human-owned**
- LLMs must not infer behavior from:
  - method names
  - class names
  - endpoint names
  - DTO/schema shapes

New behavior:
- requires explicit human declaration
- results in **new tests**
- does not modify existing test intent

---

## Test Evolution Rules

### Additive Evolution Only

- Existing tests must retain original intent
- New behavior -> new test
- Old tests are never implicitly re-scoped or reclassified

### Reclassification Policy

- Tests must **never** be reclassified automatically
- Reclassification is allowed **only** via explicit human instruction
- LLMs may suggest candidates for reclassification, but must not apply it

---

## LLM Automation Rules (Critical)

### What LLMs MAY Do Automatically

- Fix production code to satisfy failing tests
- Repair **mechanical aspects** of tests:
  - setup
  - wiring
  - fixtures
  - builders
  - data extraction
  - imports and renames

### What LLMs MUST NOT Do Automatically

- Change assertions
- Change expected values
- Remove assertions
- Add new assertions
- Broaden or weaken guarantees
- Change semantic meaning of a test

### Invariant

> **Assertions are sacred. Setup is negotiable.**

If a test cannot be repaired without changing assertions,
human intervention is required.

---

## Handling Failing Tests

Default rule:
1. A test fails
2. Assume test intent is correct
3. Fix production code

Only with explicit human instruction may the LLM:
- change test intent
- update assertions
- remove or replace tests

---

## Backend-Specific Testing Areas

### Domain Invariant Guarantees

- Treat domain invariants as primary behavioral guarantees.
- Validate invariant behavior on valid, invalid, and edge-case inputs.

### Application Orchestration Guarantees

- Validate use-case ordering, branching, and side-effect orchestration.
- Validate idempotency and retry behavior where declared.

### Contract Guarantees

- Validate API contract semantics (status/error semantics, payload meaning).
- Validate event/message contract semantics (shape, meaning, ordering rules).
- Validate persistence contract semantics (field meaning, migration compatibility).

### Failure and Recovery Guarantees

- Validate timeout/retry/dead-letter/error-translation behaviors where declared.
- Validate fallback behavior under dependency failure.

### Performance Guarantees

- Treat declared latency/throughput/SLO budgets as behavioral guarantees.
- Validate critical path budgets and regression thresholds.

### Security Behavior Guarantees

- Treat authn/authz and data-access boundaries as testable contracts.
- Validate forbidden-state behavior, input validation, and sensitive-data handling.

---

## Mocking & Real Behavior Policy

### Primary Decision Axis

> **What uncertainty is this test meant to eliminate?**

Mocking vs real behavior is decided **after** this question.

---

### Rule Correctness Tests

- Prefer **real behavior**
- Avoid mocks
- Use real implementations
- Infrastructure is part of behavior if behavior depends on it

Mocking domain logic is forbidden.

---

### Composition Tests

- Mock **trusted boundaries only**
- Each mock represents an already-tested behavior
- Assert outcomes, not implementation details

---

### Infrastructure Tests

- Use **real infrastructure** for behavior that depends on infrastructure semantics
- Keep scope narrow and deterministic

---

## Mocking Prohibitions

LLMs must not:
- mock the unit under test
- mock domain rules or validators
- mock persistence when persistence semantics are the behavior
- mock contract boundaries when contract correctness is the behavior

---

## Backend Regression Guards

When behavior is declared critical, lock it with focused tests for:
- authentication/authorization boundaries
- idempotent command semantics
- concurrency/race condition handling
- contract compatibility for clients/consumers
- failure recovery and retry behavior

Prefer focused behavior tests over broad implementation snapshots.

---

## Release Quality Gates

Minimum gate for release candidates:
- Critical backend journeys must pass end-to-end/system tests.
- Declared API/event/storage contract tests must pass.
- Declared performance/SLO budgets must pass.
- Declared security behavior guarantees must pass.
- No known failing high-severity tests may be waived without explicit human approval.

Gate ownership:
- Humans define critical journeys, budgets, and contract scope.
- LLMs execute and preserve those gates; they do not weaken them.

---

## Decision Rule for Mocking

Before mocking a dependency, ask:

> **If this dependency were wrong, would this test catch it?**

- If yes -> do NOT mock
- If no -> mocking is acceptable

---

## MVP Testing Guidance

- MVP tests focus on **minimal guarantees**
- One behavioral unit test per core capability is usually sufficient
- Tests should assert only what is explicitly promised

Behavior growth is additive.

---

## Final Invariant

> **LLMs translate declared intent into code.**
> **They do not invent intent.**
> **They do not weaken intent.**
> **They do not silently change intent.**

All automation must preserve this invariant.
