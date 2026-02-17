# Dependency Boundaries
(Import Direction & Isolation Rules for Backend Systems)

## Purpose

This document defines allowed dependency direction for backend system modules.
It prevents cyclic architecture and keeps module policy isolated from runtime concerns.

---

## Canonical Dependency Direction

`entrypoints/bootstrap -> api/infrastructure -> application -> domain -> shared`

Infrastructure direction:
- `bootstrap -> infrastructure`
- `application -> ports` (port interfaces in inner layers)
- `infrastructure -> application/ports`
- `infrastructure -> domain` is disallowed unless explicitly justified by pure mapping adapters.

Additional allowed paths:
- `api -> application -> domain`
- `api -> contracts -> application`
- `application -> contracts`
- `application -> shared`
- `domain -> shared`

Disallowed examples:
- `domain -> application`
- `domain -> infrastructure`
- `application -> api`
- `application -> infrastructure` (except by declared ports/contracts)
- `shared -> modules/<capability>/domain`
- `bootstrap -> domain` (except wiring references)

---

## Layer Roles

- `entrypoints`: process start surfaces only
- `bootstrap`: composition/wiring only
- `api`: transport-facing endpoints/controllers/handlers
- `contracts`: stable boundary DTOs/messages and module boundary schemas
- `application`: use-case orchestration and policy
- `domain`: domain rules and invariants
- `infrastructure`: external/runtime adapters
- `shared`: cross-module stable primitives/contracts

---

## Mandatory Ports and Adapters Rule

Ports/Adapters are mandatory.

Required:
- Ports define integration boundaries used by application/domain logic.
- Adapters implement ports in infrastructure modules.
- Domain/application depend on ports, never concrete adapters.

Required placement:
- Port interfaces: `modules/<capability>/application/ports/*` or `shared/ports/*`
- Adapter implementations: `modules/<capability>/infrastructure/*`

Example shape:
- `modules/billing/application/ports/InvoiceRepositoryPort.ts`
- `modules/billing/infrastructure/persistence/PostgresInvoiceRepository.ts`

---

## Runtime API Access Rule

Only infrastructure and transport-facing modules may directly call:
- ORM/database clients
- network clients
- queue/broker clients
- filesystem/object storage SDKs

Domain/application code must consume these capabilities via ports/contracts.

Only outer adapters (`api`, `infrastructure`) may depend on framework/runtime APIs.

---

## Contract Placement

- Module-specific request/response contracts: `modules/<capability>/contracts/*`
- Shared DTO/value contracts: `shared/types/*`
- Cross-module integration contracts: `shared/contracts/*`

---

## Cross-Module Interaction Policy

Allowed cross-module interaction mechanisms:
- explicit application-level ports/contracts
- published domain/application events through declared contracts
- anti-corruption/adaptation adapters in infrastructure/application boundaries

Disallowed cross-module interaction mechanisms:
- direct domain-to-domain imports between sibling modules
- bypassing contracts with deep imports into internal module folders
- sharing mutable business state through `shared/*`

Persistence-specific rule:
- Cross-capability persistence relationships must use stable references/contracts (e.g., IDs or explicit boundary contracts), not direct imports of another capability's internal persistence model types.

---

## Anti-Coupling Rules

- No cross-imports between sibling modules unless via explicit contracts/ports.
- No deep imports bypassing module boundary folders.
- No import-time side effects in `shared`.

---

## Practical Enforcement

- Review each new change for dependency direction compliance.
- Reject domain/application modules importing concrete infrastructure adapters.
- Reject shared modules accumulating capability-specific behavior.
