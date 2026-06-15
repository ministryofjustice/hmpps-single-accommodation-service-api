# Subject Access Request (SAR) Test Fixtures

This document explains how the SAR (Subject Access Request) test fixtures are structured, how the HTML report is generated using a Mustache template, and how to update the expected test fixtures when changes are made.

## Overview

The SAR implementation in SAS (Single Accommodation Service) aggregates data from the service into a single report.

- **Template**: The report is rendered using a single Mustache template located at `src/main/resources/sar/template_hmpps-single-accommodation-service-api.mustache`.
- **API Response**: The raw data returned by the SAR service is a JSON object.
- **Fixtures**: The service has its own set of "expected" fixtures (JSON and HTML) used for compliance testing. These are located in `src/test/resources/sar/`.
- **Asserter**: The `SasSarFixtureAsserter` class is used to verify the actual output against these fixtures.

## Generating Test Fixtures

When you make changes to the SAR data retrieval logic or the Mustache template, the existing tests will likely fail because the actual output no longer matches the stored "expected" fixtures.

You can automatically generate the actual output of the tests by setting the `SAR_GENERATE_ACTUAL` environment variable to `true`.

### 1. Run the tests with the generation flag

To generate new fixtures, run the following command:

```bash
SAR_GENERATE_ACTUAL=true ./gradlew integrationTest --tests "uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.sar.SarComplianceTest"
```

### 2. Locate the generated files

The tests are configured to save the actual output to `.log` files in the `src/test/resources/` directory (not in the `sar/` subdirectory) to avoid overwriting the source of truth until it's ready.

Commonly generated filenames include:
- `sas-sar-api-response.json.log`
- `sas-sar-report.html.log`

### 3. Update the expected fixtures

Once you have verified that the generated `.log` files contain the correct and expected data, copy them over the existing expected fixtures in `src/test/resources/sar/`.

**Example:**

```bash
cp src/test/resources/sas-sar-api-response.json.log src/test/resources/sar/expected_output.json 
cp src/test/resources/sas-sar-report.html.log src/test/resources/sar/expected_report.html
```

## Template Modifications

The `template_hmpps-single-accommodation-service-api.mustache` file is used by the service.

- If you add new data to the API response, you must update the template to display it.
- After updating the template, you **must** regenerate the HTML fixtures to ensure compliance tests still pass.

## Schema and Integration Tests

The `SarIntegrationTest` class contains tests for verifying the SAR template endpoints and ensuring the database schema matches expectations.

### Flyway Schema Test

The `FlywaySchemaTest` (once implemented) verifies that the Flyway migrations are up to date and match the version expected by the SAR library.

```bash
./gradlew integrationTest --tests "uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.sar.SarIntegrationTest"
```

### JPA Entities Test (Entity Schema)

The `JpaEntitiesTest` (once implemented) ensures that the JPA entities in the project correctly map to the database schema and match the expected JSON representation used by the SAR service.

If you add or modify JPA entities, you may need to update the `sas-entities-schema.json` fixture.

#### 1. Generate the new entity schema

```bash
SAR_GENERATE_ACTUAL=true ./gradlew integrationTest --tests "uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.sar.SarIntegrationTest"
```

#### 2. Update the fixture

The generated schema will be saved to `src/test/resources/entity-schema.json.log`. (The SAR test library stores the file in this location.)

#### 3. Copy it to the correct location:

```bash
cp src/test/resources/entity-schema.json.log src/test/resources/sar/sas-entities-schema.json
```