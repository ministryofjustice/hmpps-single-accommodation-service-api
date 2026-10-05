# Single Accommodation Service (SAS) API

This is the backend for the Single Accommodation Service (SAS) FE

## Run application locally

1. We need `redis` for cacheing - so you will need to run a local docker infrastructure
```shell
docker compose up -d
```

2. Decode the hmpps-single-accommodation-service-api secrets from the DEV namespace from `k8s` by running the following command:
``` bash
kubectl get secrets hmpps-single-accommodation-service-api --namespace hmpps-community-accommodation-dev -o json | jq ".data | map_values(@base64d)"
```

See `Infrastructure` section below for more information on running `kubectl`.

3. Start application locally in `IntelliJ IDEA`:
    - Create a `Run Configuration` by running  `SingleAccommodationServiceApi`
        - This will attempt start the `SAS API` Spring boot application and fail due to missing configuration
    - Edit this new `SingleAccommodationServiceApi` run configuration
        - Set the `Active profiles` field's value to `local`
        - Set the `Environment variables` field's value to:
           ```
           SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_DEFAULT_CLIENT-ID=<SYSTEM_CLIENT_SINGLE_ACCOMMODATION_SERVICE_ID>;SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_DEFAULT_CLIENT-SECRET=<SYSTEM_CLIENT_SINGLE_ACCOMMODATION_SERVICE_SECRET>
           ```
    - Swap out the `<SYSTEM_CLIENT_SINGLE_ACCOMMODATION_SERVICE_ID>` and `<SYSTEM_CLIENT_SINGLE_ACCOMMODATION_SERVICE_SECRET>` values above for the values returned from the `kubectl` command in step 2.
    - `Run` the `SingleAccommodationServiceApi` run configuration again
        - the result should be a running application (you should see in the application logs that it is deployed on port `8080`)
        - swagger documentation will also be available at `http://localhost:8080/swagger-ui/index.html`

## Run tests locally

1. We need a test database running - so you will need to run a local docker infrastructure
```shell
docker compose up -d
```

2. The following `gradle command` will build the application and run the tests
```shell
./gradlew check
```

### CPR consumer contract tests

Run the CPR contract tests without Docker, AWS or a live CPR service:

```shell
# HTTP API only
./gradlew :infrastructure:cprApiPactTest

# Asynchronous events only
./gradlew :infrastructure:cprEventPactTest

# Both (also used by the existing CI Pact workflow)
./gradlew :infrastructure:pactTest
```

Both contracts use consumer `hmpps-single-accommodation-service-api` and are generated separately
in `infrastructure/build/pacts`:

| Contract | Provider | File | Format |
| --- | --- | --- | --- |
| HTTP API | `hmpps-person-record` | `hmpps-single-accommodation-service-api-hmpps-person-record.json` | Pact V4 |
| Events | `hmpps-person-record-events` | `hmpps-single-accommodation-service-api-hmpps-person-record-events.json` | Pact V4 asynchronous messages |

Separate logical provider names keep the interactions separate in Pact Broker, not just on disk.
Separate test classes or output directories alone would still publish to the same consumer/provider
pair. The existing `pactPublish` configuration publishes both files with the same SAS app version
and branch; no additional CI job is needed.

The event contract covers address created, updated and deleted, and record updated and merged.
Event types, version and identifier type are exact values. Descriptions use a string-type matcher;
CRNs, timestamps and URLs use regex matchers; CPR address IDs use a UUID matcher. Identifier arrays
require a matching `CRN` entry but permit other entries and different ordering. `deliusAddressId`
is deliberately not required because SAS's handlers do not consume it.

The message tests pass the contract payload through the real SQS listener using an SNS envelope,
mock inbox persistence, and deserialize the saved payload to check the CRN and address identifiers
required by downstream handlers. Address deletion does not require a detail URL.

On CPR, keep the API verifier's `@Provider("hmpps-person-record")` and use
`@Provider("hmpps-person-record-events")` for the event verifier. Its broker loader or local
`@PactFolder` must select the event contract, and its `@PactVerifyProvider` descriptions must
match the event type strings above. Each event uses provider state
`CPR publishes a <eventType> event`, which must have a corresponding CPR `@State` callback.
The existing workflow dispatches CPR's verification workflow; CPR must configure that workflow
to verify both logical providers and publish results under their respective names.

The record-merged contract deliberately captures SAS's
current requirement for a `CRN` identifier; CPR currently publishes `fromCRN` and `toCRN` instead.
That mismatch needs resolving separately; these tests do not change merge handling.

If a previous local run generated a mixed API/event contract, discard the generated build output
and regenerate the separate contracts before publishing:

```shell
./gradlew :infrastructure:clean :infrastructure:pactTest
```

## Coding Notes
1. At the early stages of this project it's best to look at `CaseController.getCases()` endpoint as this follows the standards set out below implementing the correct `n-tier architecture` required (i.e. `CaseController -> CaseService -> CaseOrchestration - AggregatorService`)
2. Best practice testing standards have been included for this endpoint (inc. unit tests / integration tests)

### Standards

1. All HTTP calls to upstream services are made through the `AggregatorService`
    - The `AggregatorService` is located in the `aggregator` submodule of this repository
    - You will see examples of how this is achieved in the `CaseOrchestration` service
    - Regardless of whether you are making a number of async calls, or a single synchronous one, the standard is to make all calls through the public function in the `AggregatorService`
        - you should not need to make any changes to the `AggregatorService` — hopefully it gives you everything you need!
    - Only services in the `Orchestration layer` should inject the `AggregatorService`

2. `Orchestration layer` in `n-tier architecture`
    - Resulting flow:
      ```
      *Controller -> *Service -> *OrchestrationService -> AggregatorService
      ```
    - Example in repository:
      ```
      CaseController -> CaseService -> CaseOrchestration -> AggregatorService
      ```
    - The `Orchestration layer` will be made up of `@Service` classes named `*OrchestrationService` and their job is to:
      - Inject the `AggregatorService`
      - Help orchestrate the async (and synchronous) calls using the `AggregatorService`
      - Return an `*OrchestrationDto` data class that holds `*Dto` responses from the upstream services
      - Hard rules:
          - Only retrieve and return the data in this layer please!
          - No business-logic should sit in this layer
          - Do not unit test this layer (covered by integration tests)
      - The big plus here is that when we release a new version of the `AggregatorService` in the `aggregator` submodule it will:
          - Only have rippling effects on the `Orchestration layer`
              - We will only need to refactor implementation code in the `Orchestration layer`
              - No unit test refactoring will be neccesary
              - The integration tests will give us coverage on the `AggregatorService` itself and need no refactoring either

4. The `Service` layer that sits in between the `*Controller` and `*OrchestrationService` can be unit tested in the usual way with the `mockk` library

### Linting / Static Analysis
* There are linting and static analysis checks in the build pipeline. You can lint and check for issues by running

```bash
./gradlew ktlintFormat && ./gradlew detekt
```

## Infrastructure

The service is deployed to the [MoJ Cloud Platform](https://user-guide.cloud-platform.service.justice.gov.uk). This is
managed by Kubernetes and Helm Charts which reside within this repo at [`./helm_deploy`](./helm_deploy/approved-premises-api/).

To get set up with Kubernetes and configure your system so that the `kubectl` command authenticates, see this
[[MoJ guide to generating a 'kube' config](https://user-guide.cloud-platform.service.justice.gov.uk/documentation/getting-started/kubectl-config.html#generating-a-kubeconfig-file)].

You should then be able to run `kubectl` commands, e.g. to list the 'pods' in a given 'namespace':

```bash
$ kubectl -n hmpps-community-accommodation-dev get pods

NAME                                                     READY   STATUS    RESTARTS   AGE
hmpps-single-accommodation-service-api-655968557b-5qlbc  1/1     Running   0          83m
hmpps-single-accommodation-service-api-655968557b-bp7v9  1/1     Running   0          83m
hmpps-single-accommodation-service-ui-67b49b8dcd-p85pt   1/1     Running   0          125m
hmpps-single-accommodationn-service-ui-67b49b8dcd-tgjd5  1/1     Running   0          125m
```
**NB**: this [`kubectl` cheatsheet](https://kubernetes.io/docs/reference/kubectl/cheatsheet/) is a good reference to
other commands you may need.

### Environments

[Details of the different environments and their roles can be found in
Confluence](https://dsdmoj.atlassian.net/wiki/spaces/AP/pages/5001478252/CAS+Environments).

## Release process

Our release process aligns with the other CAS teams and as such [lives in Confluence](https://dsdmoj.atlassian.net/wiki/spaces/AP/pages/4247847062/Release+process).
The steps are also available in the pull request checklist[PULL_REQUEST_TEMPLATE](/.github/PULL_REQUEST_TEMPLATE/full_template.md).

## Rules Engine

The eligibility rules engine is in the codebase, you can extract a markdown version, (complete with mermaid diagrams) of the rules engine by running the following command:

```bash
./gradlew :query-service:generateRulesEngineMarkdown
```

the file generated is viewable [here](./docs/eligibility-rules-graph.md)