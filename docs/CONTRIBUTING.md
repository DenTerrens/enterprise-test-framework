# Contributing Guide

## Principles

- I prefer readable, explicit abstractions over clever framework code.
- I keep selectors, endpoints, SQL, and file logic isolated from test intent.
- I avoid hardcoded environment values in tests.
- I keep sample tests realistic enough to demonstrate business value.

## Conventions

- Use `@Tag` annotations consistently so suites remain easy to target.
- Keep page objects action-oriented and avoid test assertions inside them.
- Keep API services focused on request execution, not business assertions.
- Keep DB queries readable and small; use helper methods when reuse emerges.
- Prefer fixture files in `src/test/resources` over large inline payloads.

## Pull request checklist

- Does the change preserve clear layer boundaries?
- Are config values externalized?
- Are new tests tagged correctly?
- Are Allure attachments still meaningful?
- Has documentation been updated if the workflow changed?
