# Architecture

## Design goals

This framework is optimized for maintainability, demo value, and extension. The architecture favors clear boundaries over clever abstractions.

## Key decisions

- Maven was kept as the build backbone because the framework spans Playwright Java, RestAssured, JDBC utilities, JMeter, and Allure.
- JUnit 5 replaces TestNG because it provides modern tagging, extensions, parallel execution, and clean integration with Maven Surefire.
- Playwright Java is used only for browser automation. JUnit 5 handles orchestration, lifecycle, fixtures, and tagging.
- The API layer is intentionally split into a transport client and domain service so auth, headers, and request behavior stay reusable.
- The database layer uses plain JDBC for transparency and low framework overhead.
- File verification utilities stay lightweight and readable instead of introducing a large verification DSL.

## Layer breakdown

- `config`: reads base and environment-specific properties and supports runtime overrides with `-D` flags.
- `ui.playwright`: owns browser lifecycle, context creation, and page exposure.
- `ui.pages`: page objects that encapsulate selectors and user actions.
- `api.client`: generic HTTP client setup, auth handling, and shared request configuration.
- `api.service`: domain-oriented API methods used directly by tests.
- `db`: query, update, setup, and integrity helpers for backend assertions.
- `files`: parsers and assertions for text, CSV, TSV, JSON, and XML.
- `reporting`: Allure attachment utilities used by the JUnit watcher.
- `tests.base`: reusable setup and teardown for UI, API, and DB tests.

## End-state package structure

```text
src/main/java/com/automation/framework
|-- api
|   |-- client
|   `-- service
|-- config
|-- db
|-- files
|-- reporting
|-- ui
|   |-- pages
|   `-- playwright
`-- utils

src/test/java/com/automation/framework/tests
|-- api
|-- base
|-- db
|-- files
|-- integration
`-- ui
```

## UI design choices

- Page objects are used for readability and maintainability.
- Stable selectors are preferred, especially `data-test` attributes on the sample UI flow.
- Browser lifecycle is thread-safe through `ThreadLocal` Playwright resources.
- Failure evidence is captured before teardown so screenshots and DOM snapshots survive failed UI tests.

## API design choices

- The API client supports config-driven auth strategies: `none`, `basic`, and `bearer`.
- CRUD examples demonstrate GET, POST, PUT, and DELETE.
- Schema files live under `src/test/resources/data/api` so contract checks remain versioned and reviewable.
- Negative and idempotency scenarios are treated as first-class API checks instead of afterthoughts.

## Data and environment strategy

- Base config lives in `src/test/resources/config/application.properties`.
- Environment overrides live in `src/test/resources/config/environments/<env>.properties`.
- Runtime overrides are passed with Maven system properties such as `-Denv=qa`, `-Dbrowser=firefox`, or auth overrides for protected APIs.
- Request payloads, schema files, and file fixtures live under `src/test/resources/data`.
- DB schema and seed data live under `src/test/resources/db`.

## SQL and cross-system strategy

- H2 keeps the sample project runnable locally while still demonstrating JDBC-based backend verification.
- Each DB test gets repeatable setup via schema and seed scripts.
- Integration scenarios persist API-derived data into an audit table to demonstrate cross-system verification.
- Integrity checks such as uniqueness constraints are shown directly in the DB suite.

## Parallel execution strategy

- JUnit 5 parallel execution is enabled through `junit-platform.properties`.
- UI execution is thread-safe via `ThreadLocal` Playwright resources.
- API and file tests are naturally parallel-safe.
- DB tests use isolated in-memory H2 setup per test lifecycle.

## CI/CD strategy

- Functional suites run through a matrix job for clearer failure isolation.
- UI smoke runs separately as a visible browser-focused gate.
- Performance runs on a schedule and on demand, with JMeter artifacts and summary output.
- Pages publishing is treated as a publishing workflow, not a core quality gate.
- Secrets should be injected through GitHub Actions repository or environment secrets and mapped to system properties at runtime.

## Tradeoffs and future improvements

- Public demo systems keep the framework easy to run, but they are less stable than internal test environments.
- JSONPlaceholder is useful for showcase CRUD flows, but a dedicated mock service would allow richer negative cases.
- Larger projects may benefit from richer API models and a component-object layer on the UI side.
- Future expansion should include stronger contract coverage, secret examples in CI, and deeper data lifecycle automation.
