# AGENT.md — Guidelines for AI coding assistants

This document sets expectations for AI assistants (IDE agents, Copilot-style tools, batch agents) working in this repository. Read it before changing architecture or Gradle dependencies. Human-facing deep dives also live under `docs/` (English and `*.zh-CN.md` pairs).

---

## 1. What this project is

- **Stack**: Quarkus, **JDK 25** (see `gradle/libs.versions.toml`), PostgreSQL, Redis/Valkey, JWT, Flyway via **`apps:migrator`**.
- **Goal**: A reusable **RBAC + identity** backend template with DDD-style multi-module layout and multiple processes (admin vs mobile) sharing domain code.

Treat **`gradle/libs.versions.toml`** as the source of truth for versions; do not invent version numbers in answers.

---

## 2. Repository map (three layers)

| Layer | Gradle prefix | Role |
|-------|---------------|------|
| Shared kernel & technical building blocks | `libs:*` | Entities, Panache, Redis stores, security primitives, **`libs:rest-support`** (shared JAX-RS exception mapping, refresh-token cookies) |
| Application / bounded context | `modules:*` | Use cases, ports, Quarkus-oriented wiring (`identity`, `accesscontrol`, `security-runtime`, `example-ddd`) |
| Deployable HTTP adapters | `apps:*` | JAX-RS, `application.yaml`, which `modules` are on the classpath |

**Dependency flow (mandatory)**: `apps` → `modules` → `libs`. Avoid arbitrary cycles between `modules`; only connect where the design explicitly allows (e.g. **`modules:security-runtime`** depends on **`modules:identity`**).

---

## 3. Hard rules (confirm with the user before violating)

### 3.1 `apps:mobile-api` dependency whitelist

- **Intended dependencies**: `libs:common`, `libs:rest-support`, `modules:identity`, `modules:security-runtime`, plus Quarkus bundles as declared in **`apps/mobile-api/build.gradle.kts`**.
- **Do not add by default**: `modules:accesscontrol`, `modules:example-ddd`. If the product truly needs RBAC admin APIs or the sample domain on the C-side, discuss threat model and API surface with the user first.  
  Details: **`docs/MOBILE_API.md`**.

### 3.2 Example module port packages

- **Inbound ports** (driving adapters depend only on these interfaces): under **`application.port.in`** in **`modules:example-ddd`** (e.g. `ExampleProductCatalogApi`).
- **Driven ports** (implemented by infrastructure): **`application.port.driven`**.  
  **Never** use a package or path segment named **`out`** (e.g. `port.out`): it collides with common **`.gitignore`** rules for `out/` and can leave source untracked.

### 3.3 HTTP layout and cross-cutting

- Admin REST: under **`apps/admin-api`**, package **`com.github.DaiYuANg.modules.*`** (`identity`, `accesscontrol`, `example`).
- Mobile REST: **`com.github.DaiYuANg.mobile.identity.*`**.
- Shared JAX-RS concerns: use **`libs:rest-support`**; do not duplicate **`GlobalExceptionHandler`** per app.

### 3.4 Database and migrations

- Apps often use **`hibernate-orm.schema-management.strategy: validate`**. Schema changes belong in **Flyway** scripts run from **`apps:migrator`**; do not assume Hibernate will alter production schemas automatically.

---

## 4. Recommended workflow for new features

1. **Bound the change**: Which bounded context? Admin-only, mobile, or both?
2. **Choose Gradle homes**: Entities/repos → **`libs:<context>`** (or a new `libs` project); use cases and ports → **`modules:<context>`**; HTTP and YAML → **`apps/...`** only.
3. **Ports** (in `example-ddd` or similar): driving adapters depend on **`port.in`**; persistence/Redis/external systems → interfaces in **`port.driven`**, implementations under **`infrastructure`**.
4. **Config**: Identity split → **`app.identity.*`** (admin vs mobile `userType`); replay headers → **`app.replay.*`** and **`@ReplayProtected`** where used.
5. **Contracts**: If login, `/me`, or the shared **`Result`** envelope shape changes, update **`AdminIdentityRestJsonContractTest`** / **`MobileIdentityRestJsonContractTest`** (REST Assured) or explicitly treat it as a breaking API change with the user.

---

## 5. Style, language level, and Lombok

### 5.1 Scope

- **Minimal diff**: Touch only files needed for the task; no drive-by refactors, comment deletion, or unrelated formatting.
- **Match neighboring code**: package layout, naming, and patterns in the same module.

### 5.2 Modern JDK syntax (align with the toolchain)

- The project uses **JDK 25** (see **`gradle/libs.versions.toml`**). Prefer **current Java idioms** over legacy style when they stay readable:
  - **`record`** for immutable carriers (DTOs, command objects, small value types) where JPA or frameworks do not forbid it.
  - **Pattern matching** (`instanceof`, `switch`), **`sealed`** hierarchies, and **exhaustive `switch`** where they clarify domain branches.
  - **Text blocks**, **`var`** when the inferred type is obvious, **`Optional` / `Stream`** usage consistent with nearby code.
  - Language features already adopted in the repo (e.g. **flexible constructor bodies** on compact constructors) are fair game when they reduce noise.
- **Preview / incubating** features: only with explicit toolchain/compiler flags and agreement with the user; do not silently depend on unstable language modes.
- Avoid unnecessary verbosity: no manual `close()` where try-with-resources fits; no anonymous singleton `Runnable` where a lambda or method reference is clearer.

### 5.3 Lombok (prefer high-level / current idioms)

- Use **Lombok** to replace repetitive boilerplate, consistent with the **Remal Lombok** setup in this repo (see **`gradle/libs.versions.toml`** / root build plugins).
- **Defaults in this codebase** — mirror the same annotations as sibling classes:
  - **`@RequiredArgsConstructor(onConstructor_ = @Inject)`** on CDI beans (REST resources, application services, adapters).
  - **`@Getter`**, **`@Slf4j`**, and other annotations **only when** the surrounding package already uses them for the same kind of type.
  - **Records + Lombok**: use **`@Builder`** on records or small types when the module already does; prefer **`record`** + compact canonical constructor over mutable POJOs when persistence mapping allows.
- Prefer Lombok-generated **constructors / getters / loggers** over hand-written copies that duplicate the same pattern across files.
- **Do not** mix styles in one class (e.g. half manual getters, half `@Data`) without a reason; follow the dominant pattern in that package.
- If a newer Lombok feature is tempting but unused elsewhere in the repo, **default to existing idioms** unless the user asks for an upgrade path.

---

## 6. Tests and local verification

- Prefer **`./gradlew :apps:admin-api:test`**; add **`:apps:mobile-api:test`** when mobile behavior is affected.
- Contract tests rely on Testcontainers (PostgreSQL) and a **Valkey** test resource; on failure, check Docker/network before weakening assertions.
- After broad edits, **`./gradlew check`** is appropriate (Checkstyle, SpotBugs, etc., per project config).

---

## 7. Documentation index

| Topic | Path |
|-------|------|
| Design, Mermaid diagrams, stack | `docs/PROJECT_DESIGN.md` (and `docs/PROJECT_DESIGN.zh-CN.md`) |
| Directory layout, example ports | `docs/ARCHITECTURE_DDD.md` (and `.zh-CN`) |
| Mobile whitelist and cookies | `docs/MOBILE_API.md` (and `.zh-CN`) |
| Authorization and permission snapshots | `docs/AUTHORIZATION_FLOW.md` (and `.zh-CN`) |
| Doc hub | `docs/README.md` |

---

## 8. Commits

- Use a clear subject describing **behavior**; body bullets are welcome. Avoid meaningless subjects like “update” or “fix” with no scope.
- Do not commit **`build/`**, local secrets, or **`gradle.properties`** if it is gitignored for secrets/local JVM opts.

---

## 9. Pre-flight checklist

- [ ] Are new dependencies on the correct layer (`libs` / `modules` / `apps`)?
- [ ] Did `mobile-api` accidentally gain `accesscontrol` or `example-ddd`?
- [ ] Did you avoid `out` as a Java package path for ports?
- [ ] If JSON contracts changed, did you update contract tests or flag a breaking change?
- [ ] Do schema changes need a Flyway script and a note for **`migrator`**?
- [ ] Is new DB access behind a repository (or driven port) and using **`Entity_` / `Q*`** instead of fragile string field names where possible?
- [ ] For new reads, is the approach **`@HQL`** (simple), existing **Blaze/QueryDSL** (dynamic lists), or isolated **Doma** (complex SQL) per **§10.4–10.6**?
- [ ] Does every new external dependency follow **§11** (Quarkus first, then vetted libraries, then minimal custom code)?
- [ ] Does new code use **JDK 25–style** constructs and **Lombok** the same way as neighboring classes (**§5.2–5.3**)?
- [ ] Are new **Gradle** tasks or heavy build logic in **`buildSrc`**, and local deps using **`projects.*`** accessors (**§12**)?
- [ ] For new endpoints: **security annotations**, **no secrets in logs**, **validation**, and **transactions** consistent with **§13**?

---

## 10. Data access layer

### 10.1 Repository pattern (preferred)

- **Default**: expose persistence through **repositories**, not through JAX-RS resources or ad-hoc `EntityManager` calls scattered across `modules` / `apps`.
- **Command-style repos**: extend **`BasePanacheCommandRepository<E>`** (Panache + shared `findByIdOrThrow` / `save` / `remove` semantics). Example: `OperationLogRepository`, `UserRepository` base behavior.
- **Queries**: keep list/page/search logic in dedicated types (e.g. `*QueryRepository`, Blaze + QueryDSL helpers under `libs:<context>`) consistent with existing **`UserRepository`** / **`MetamodelUserQueryBuilder`** patterns.
- **Application layer** should depend on repository (or port) APIs; avoid leaking Panache entity types into HTTP DTOs without mapping.

### 10.2 Strong typing and Hibernate-generated metadata

- Prefer **type-safe** references to persistent attributes instead of raw string property names in queries, sorts, and criteria.
- This repo uses the **`hibernate-processor`** annotation processor (see **`gradle/libs.versions.toml`** and **`libs.bundles.persistence.annotation.processor`**). It generates JPA static metamodel classes named **`Entity_`** (e.g. `SysUser_`, `BaseEntity_`) under build `generated/sources/annotationProcessor`.
- **Use `Entity_` fields** where the codebase already does: for example sort mapping via **`MetamodelSortMapping`** and **`SysUser_.username`**, **`BaseEntity_.createAt`**, etc. Do not reintroduce fragile string keys for the same attributes when `_` types exist.
- **QueryDSL `Q…` types** are also part of the strong-typed query stack (Blaze JPA queries). Follow the same module’s existing mix: processor deps mirror **`libs/identity/build.gradle.kts`** or **`libs/accesscontrol/build.gradle.kts`** (`querydsl-apt` **Jakarta** classifier + **`persistence.annotation.processor`** bundle).

### 10.3 New `libs` modules that map entities

When you add a new library with JPA entities:

1. Depend on **`projects.libs.persistence`** (and Blaze/QueryDSL like sibling libs if you need the same query style).
2. Register **`annotationProcessor(libs.bundles.persistence.annotation.processor)`** so **`Entity_`** is generated for each `@Entity` / mapped superclass.
3. If you use QueryDSL **`Q*`** entities, add the same **`querydsl-apt`** **Jakarta** `compileOnly` + `annotationProcessor` lines as in **`libs/identity/build.gradle.kts`**.
4. Prefer **repositories + metamodel-backed builders** over inline JPQL strings for anything non-trivial.

### 10.4 Simple read queries: Hibernate processor `@HQL` interfaces

- For **simple** reads (straight filters, small joins, lookups that fit comfortably in one HQL/JPQL string), prefer **repository-style interfaces** annotated with **`@HQL`** from **`org.hibernate.annotations.processing`**, processed by the existing **`hibernate-processor`** (same processor that generates **`Entity_`**).
- **Scope**: use **`@HQL`** only where the query stays **simple**—avoid stuffing large, dialect-specific, or deeply nested SQL logic into HQL strings. Those belong under **§10.5** or the existing **Blaze + QueryDSL** patterns for dynamic list/search screens.
- Wire generated implementations like any other CDI bean (follow Hibernate Data Repositories / processor docs for the version pinned in **`gradle/libs.versions.toml`**). Map results to DTOs or domain views before HTTP layers.

### 10.5 Complex reads: optional **Doma** as an isolated SQL query layer

- When a query is **too complex** for **`@HQL`** or the existing **Blaze + QueryDSL + metamodel** approach (e.g. heavy reporting, window functions, vendor-specific SQL, or very large static SQL), you may introduce **[Doma](https://doma.readthedocs.io/)** strictly as a **read / pure-SQL query layer**.
- **Quarkus integration**: Doma ships **first-party Quarkus support**—use the documented extension and setup from **[Doma Quarkus support](https://doma.readthedocs.io/quarkus-support/)** (installing, CDI, datasource, build config). Do **not** hand-wire Doma to raw JDBC alongside Quarkus when the official integration covers the need (**§11.1**).
- **Isolation rules**:
  - Keep Doma in a **narrow place**: e.g. a dedicated **`libs:*`** slice or an **`infrastructure/doma`** (or similar) package—not mixed through every Panache entity.
  - Use it for **query paths**; **commands** (insert/update/delete and transactional aggregates) should still go through the **repository / Panache / JPA** style unless the user explicitly approves a wider adoption.
  - Return **DTOs or projections**, not leak Doma SQL types into **`modules`** application APIs.
- Add the Doma version to **`gradle/libs.versions.toml`** and depend via **`libs.*`** (**§11.2**). Confirm compatibility with the project’s JDK and Quarkus/Hibernate stack before merging.

### 10.6 Choosing a query style (summary)

| Situation | Prefer |
|-----------|--------|
| Simple static HQL-shaped read | **`@HQL`** interfaces + **`hibernate-processor`** (**§10.4**) |
| Dynamic paging / filters already in the module | Existing **Blaze + QueryDSL + `Entity_` / `Q*`** (**§10.2**) |
| Complex or SQL-centric read | **Doma**, isolated (**§10.5**) |

### 10.7 Scope note

- **`modules:example-ddd`** may use Panache on entities inside the module for the sample; still keep HTTP out of direct persistence and align with **`port.driven`** boundaries. For greenfield features in core `libs`, treat **§10.1–10.3** as the default bar, then **§10.4–10.5** when adding new query technology.

---

## 11. Third-party dependencies (minimize and prefer the platform)

**Goal**: keep the dependency surface small, aligned with Quarkus versions, and easy to upgrade.

### 11.1 Decision order (strict preference)

1. **Quarkus built-ins and extensions** — Use what **`io.quarkus:*`** and the **Quarkus BOM** already provide (REST, security, Hibernate, Redis, validation, observability, etc.). Check [Quarkus extensions](https://quarkus.io/extensions/) and, when relevant, **Quarkiverse** before adding a random Maven artifact.
2. **Actively maintained, widely used libraries** — If Quarkus has no fit, prefer **stable ecosystem** dependencies that are already common in enterprise Java: examples in this repo include **Guava** and **Apache Commons** family (e.g. **`commons-codec`**). Favor libraries with clear maintenance, semantic versioning, and a small transitive tree.
3. **Implement in-repo** — Only when (1) and (2) do not cover the need, add a **small, focused** helper in an appropriate **`libs:*`** module (often **`libs:common`** for pure utilities). Do **not** roll your own crypto, auth protocols, or security-sensitive parsers unless there is no vetted alternative and the user explicitly accepts the risk.

### 11.2 When adding a new dependency

- Add it to **`gradle/libs.versions.toml`** (version catalog) instead of hard-coding versions in subprojects, unless the project already uses a different local convention.
- Prefer **BOM-managed** versions where Quarkus or an existing BOM already defines them.
- Mention **why** Quarkus (or an existing stack library) was insufficient, in the PR / commit body if the change is non-obvious.
- **Doma** (see **§10.5**): only for the **isolated read / SQL query layer**; use Doma’s **official Quarkus extension** and guide ([Doma Quarkus support](https://doma.readthedocs.io/quarkus-support/)) instead of custom JDBC glue, and keep the module boundary as documented in **§10.5**.

### 11.3 What to avoid

- Duplicating framework features (another HTTP client, another JSON mapper, another config layer) when Quarkus already ships one.
- Tiny unmaintained “utility” libraries with unclear ownership or overlapping functionality with **Commons** / **Guava** / JDK APIs.

---

## 12. Gradle build scripts

### 12.1 Custom tasks and build logic live in `buildSrc`

- **New custom tasks** (anything beyond standard plugin configuration): implement as **Kotlin task types** or **convention plugins** under **`buildSrc/src/main/kotlin`** (same pattern as **`GenerateRsaKeysTask`**, **`ReplacePackageTask`**, **`VerifyLayerDependenciesTask`**).
- **Root `build.gradle.kts` and subproject scripts** should stay **thin**: `plugins { }`, `dependencies { }`, and short `tasks.register<YourTask>("name")` wiring only—**not** large imperative build scripts or task bodies inlined in every module.
- Shared configuration that repeats across many subprojects should move toward **`buildSrc`** (precompiled script plugins or `buildSrc` helpers), not copy-paste across `apps/*/build.gradle.kts`.

### 12.2 Strongly typed local project dependencies

- **`settings.gradle.kts`** enables **`enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")`**. Use generated accessors for **every** dependency on another Gradle project:
  - **`projects.libs.common`**, **`projects.libs.restSupport`**, **`projects.modules.identity`**, **`projects.modules.exampleDdd`**, etc.
  - **Do not** use string paths such as **`project(":libs:rest-support")`** or **`project(":modules:example-ddd")`** for new code when a **`projects.*`** accessor exists (kebab-case path segments become **camelCase** in the accessor name, e.g. **`libs:rest-support`** → **`projects.libs.restSupport`**).
- **External** coordinates belong in **`gradle/libs.versions.toml`** and are referenced as **`libs.quarkus.bom`**, **`libs.bundles.quarkus.test`**, etc.—avoid scattered literal version strings in subprojects.

### 12.3 Adding a new included project

- **`include("libs:your-lib")`** in **`settings.gradle.kts`** first; then depend with **`projects.libs.yourLib`** (adjust camelCase to match Gradle’s generated name—verify in IDE or a dry **`./gradlew projects`** if unsure).

---

## 13. Additional conventions (recommended)

These are easy for agents to miss; follow them unless the user explicitly overrides.

### 13.1 Security and configuration

- **Secrets**: never commit keys, passwords, or production connection strings. Use **environment variables** / Quarkus **`%prod`** profiles and documented placeholders. JWT key generation stays with **`generateRsaKeys`** (or your ops pipeline), not hard-coded PEM in YAML.
- **Logs**: do not log **passwords**, **refresh tokens**, full **JWT** payloads, or other high-sensitivity data. Prefer opaque ids and **`ResultCode`**-style diagnostics.
- **New HTTP endpoints**: align with **Quarkus Security** (`@RolesAllowed`, `@PermitAll`, `@Authenticated`) the same way as sibling resources. For admin RBAC, permission strings and enforcement should stay consistent with **`docs/AUTHORIZATION_FLOW.md`**.

### 13.2 Transactions and consistency

- Use **`@Transactional`** on **application services** or repository entry points where the codebase already does for multi-step persistence. Avoid long transactions holding connections across remote I/O unless intentional.

### 13.3 API surface and compatibility

- Keep **path prefixes** stable (`/api/v1/...`, `/api/mobile/v1/...`). **Breaking** JSON or status-code changes require contract-test updates (**§4**) or an explicit **versioned** path / agreement with the user.
- Prefer **Bean Validation** (`@Valid`, constraints on DTOs) on new request bodies for consistent **400** responses.

### 13.4 Performance and data loading

- Watch for **N+1** access when adding relations; follow existing **Blaze / Entity View / fetch** patterns in the same module rather than lazy-loading loops in hot paths.
- List endpoints should use **existing pagination** (`PageQuery` / slice types) when listing can grow large.

### 13.5 Formatting and static analysis

- After substantive edits, run **`./gradlew spotlessApply`** when Java/Kotlin style may have drifted; ensure **`./gradlew check`** (or at least affected-module tests) still passes before claiming work is done.

### 13.6 Observability (when touching request paths)

- For new significant entry points, consider **structured logging** context and **metrics/tracing** hooks consistent with Quarkus defaults (`quarkus.log`, OpenTelemetry) rather than ad-hoc `System.out`.

---

## 14. Further ideas (not yet mandatory)

Consider promoting these into hard rules later if the team agrees:

| Area | Possible rule |
|------|----------------|
| **OpenAPI** | Require SmallRye annotations on new public REST types when Swagger is part of your contract. |
| **Redis keys** | Namespace + TTL documented next to new cache stores (avoid unbounded growth). |
| **Error messages** | Separate **operator-facing** (English/log) vs **end-user** localized copy if the product ships i18n. |
| **Package rename** | Template consumers: use **`replacePackage`** / **`buildSrc`** tasks instead of manual find-replace. |
| **CI** | Every PR runs **`check`** + Docker-based tests; document required services in **`docs/TROUBLESHOOTING`**. |

---

*If this file disagrees with the repo, trust the current Gradle layout and `docs/`, then update **`AGENT.md`**.*
