# Troubleshooting

## Playwright browser install issues

The Playwright Java dependency downloads browser binaries on first use. If the first run fails in a restricted environment, rerun when browser download access is available.

## Smoke or regression workflow failed on formatting

Run:

```bash
mvn spotless:apply
```

Then rerun:

```bash
mvn spotless:check
```

## Allure report is empty

Run tests before generating the report and confirm `allure-results` contains files. The persistent HTML report is generated in `allure-report`.

## UI failure has no screenshot or video

Run a failing UI test after `mvn clean test`. Evidence is attached only when the failure happens while the Playwright session is active. Failure-demo mode is a good quick check:

```bash
mvn clean test -Pfailure-demo
```

## Public demo site instability

Some smoke examples use public demo systems. If those systems are unavailable, prefer the local integrated `demo` group for deterministic execution.

## DB verification failures

The DB suites expect schema and seed scripts under `src/test/resources/db`. If you rename or reorganize those files, update the base database setup classes.

## GitHub Pages deployment blocked

If `pages-allure-report` fails with a Pages environment restriction, open `Settings -> Environments -> github-pages` and make sure your active branch is allowed to deploy.

## Tag filtering did not work

Use Maven Surefire tag properties:

```bash
mvn clean test -Dgroups=smoke
mvn clean test -Dgroups=demo
mvn clean test -DexcludedGroups=integration
```
