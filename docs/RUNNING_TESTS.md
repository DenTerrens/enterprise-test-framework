# Running Tests

## Full regression

```bash
mvn clean test
```

This runs the default regression coverage and excludes the intentional failure-demo tests.

## Smoke suite

```bash
mvn clean test -Dgroups=smoke
```

Use this when you want a fast signal for core UI and API behavior.

## By layer

```bash
mvn clean test -Pui
mvn clean test -Papi
mvn clean test -Pdb
mvn clean test -Pfiles
mvn clean test -Pintegration
```

## Integrated local demo flows

```bash
mvn clean test -Dgroups=demo
```

This runs the embedded demo app scenarios that prove:

- UI create -> API + DB verification
- API update -> UI + DB verification
- UI file upload -> API + DB verification

## Browser and environment overrides

```bash
mvn clean test -Pui -Dbrowser=firefox -Dheadless=false
mvn clean test -Denv=qa
```

## API auth overrides

The framework supports `none`, `basic`, and `bearer` auth strategies through Maven properties or CI secrets.

```bash
mvn clean test -Papi -Dapi.auth.type=bearer -Dapi.auth.token=my-token
mvn clean test -Papi -Dapi.auth.type=basic -Dapi.auth.username=user -Dapi.auth.password=pass
```

## Allure reports

```bash
mvn clean test
mvn allure:report
mvn allure:serve
```

Artifacts are written to:

- `allure-results`
- `allure-report`
- `reports/surefire`
- `logs/automation-framework.log`

## Intentional failure-demo mode

These tests exist only to show what failed runs look like in Allure.

```bash
mvn clean test -Pfailure-demo
```

Optional filtering:

```bash
mvn clean test -Pfailure-demo -Dgroups=ui,demo-failure
mvn clean test -Pfailure-demo -Dgroups=api,demo-failure
```

## Formatting and lint-style checks

```bash
mvn spotless:check
mvn spotless:apply
```

## JMeter

```bash
jmeter -n -t performance/jmeter/plans/reqres-smoke.jmx -l performance/jmeter/results.jtl -e -o performance/jmeter/report
```

Use RestAssured for functional correctness and workflow validation. Use JMeter for concurrency and response-time observations.

## CI behavior

- `smoke-suite` runs on push, pull request, and manual dispatch
- `regression-suite` runs on push and pull request
- `performance` runs weekly and manually
- `pages-allure-report` runs manually or when meaningful framework files change
