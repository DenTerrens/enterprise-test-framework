# Architecture

## Design intent

This framework is built to show system thinking, not just test scripting. The architecture separates transport, browser, data, reporting, and test intent so the same codebase can support smoke checks, deeper regression, and cross-layer verification without turning into a pile of brittle helpers.

## Why it is structured this way

- `Maven + JUnit 5` keeps the execution model simple, familiar, and CI-friendly
- `Playwright Java` handles browser automation only; orchestration stays in JUnit
- `RestAssured` is wrapped behind service classes so tests do not own HTTP plumbing
- `JDBC` is used directly for transparency and explainability in interviews
- `Allure` is kept at the framework boundary so tests remain focused on behavior rather than attachment code

## Layer breakdown

- `config`
  Reads application defaults plus environment overrides and allows `-D` property overrides at runtime.

- `ui.playwright`
  Owns thread-local Playwright lifecycle, browser context setup, headless selection, and video recording.

- `ui.pages`
  Encapsulates selectors and browser actions. External smoke coverage and the local integrated demo use separate page objects where the domains differ.

- `api.client`
  Centralizes base URI setup, shared headers, Allure API logging, and auth strategy selection.

- `api.service`
  Exposes business-level API actions such as login, user CRUD, and upload lookup.

- `db`
  Provides reusable query/update access through a thin JDBC client.

- `files`
  Contains file parsing and assertion helpers for text, CSV, TSV, JSON, and XML.

- `reporting`
  Provides Allure attachment helpers for text, HTML, screenshots, and file-based evidence.

- `demoapp`
  Hosts a lightweight embedded demo system used for local integrated UI/API/DB verification.

## Test architecture

There are three main styles of tests in this repo:

- Smoke tests
  Fast checks for confidence on push and pull request. These are tagged with `smoke`.

- Regression tests
  Broader coverage across UI, API, DB, files, and integrated flows. These are tagged with `regression`.

- Failure-demo tests
  Intentionally failing scenarios used only for reporting demonstrations. These are excluded from default runs.

## UI strategy

- Use page objects, not locator sprawl inside tests
- Prefer stable `data-test` selectors wherever we control the UI
- Keep browser lifecycle thread-safe through `ThreadLocal`
- Capture screenshot, DOM, and video evidence on failure
- Avoid blanket sleeps; use Playwright waits and response-based synchronization

## API strategy

- Separate HTTP transport from business-level services
- Support `none`, `basic`, and `bearer` authentication through configuration
- Demonstrate GET, POST, PUT, PATCH, and DELETE
- Store JSON payloads and schemas under versioned test resources
- Treat negative cases and idempotency as first-class API coverage

## SQL and data strategy

- Keep the DB layer small and explicit so query intent is easy to review
- Use schema and seed scripts for deterministic local setup
- Use polling-based verification only where eventual consistency would be realistic
- Demonstrate both integrity checks and cross-layer verification

## Cross-layer strategy

The embedded demo app exists so the framework can prove real end-to-end thinking:

- create via UI -> verify via API and DB
- update via API -> verify via UI and DB
- upload file via UI -> verify API output and DB audit state

That is more valuable in a portfolio than dozens of disconnected single-layer tests.

## Configuration model

- Base properties: `src/test/resources/config/application.properties`
- Environment overrides: `src/test/resources/config/environments/<env>.properties`
- Runtime overrides: Maven `-D` properties
- Example secret/env names: `.env.example`

## CI/CD model

- `smoke-suite`
  Fast push/PR gate for `smoke` coverage

- `regression-suite`
  Broader push/PR gate for `regression` coverage

- `performance`
  Weekly/manual JMeter execution with artifacts and summary output

- `pages-allure-report`
  Publishing workflow that generates and deploys the Allure HTML report

## Tradeoffs

- Public demo systems are useful for portability, but they are not as controllable as internal test environments
- The embedded local demo app increases portfolio value, but it is intentionally lightweight and not a substitute for a real staging environment
- H2 keeps setup fast and portable, but production database behavior can differ by engine
