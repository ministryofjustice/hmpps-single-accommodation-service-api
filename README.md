# Single Accommodation Service (SAS) API

This is the backend for the Single Accommodation Service (SAS) FE

## Run application locally

1. We need a `redis` for cacheing - so you will need to run a local docker infrastructure
```shell
docker compose up -d
```

2. Start application locally in `IntelliJ IDEA`:
    - Create a `Run Configuration` by running  `SingleAccommodationServiceApi`
        - This will attempt start the `SAS API` Spring boot application and fail due to missing configuration
    - Edit this new `SingleAccommodationServiceApi` run configuration
       - Set the `Active profiles` field's value to `local`
       - Set the `Environment variables` field's value to:
          ```
          SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_DEFAULT_CLIENT-ID=<secret-value>;SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_DEFAULT_CLIENT-SECRET=<secret_value>
          ```
    - Swap out the `<secret-value>` values above for the actual secrets held in `k8s secrets` - see `Infrastructure` section below for how to run `k8s` commands 
    - `Run` the `SingleAccommodationServiceApi` run configuration again
        - the result should be a running application (you should see in the application logs that it is deployed on port `8080`)

## Run tests locally
1. No local docker infrastructure required and using lib for embedded redis (for tests)
2. The following `gradle command` will build the application and run the tests
```shell
.gradlew check
```

## Coding Notes
1. At the early stages of this project it's best to look at `CaseController.getCases()` endpoint as this follows the standards set out below implementing the correct `n-tier architecture` required (i.e. `CaseController -> CaseService -> CaseOrchestration - AggregatorService`)
2. Best practice testing standards have been included for this endpoint (inc unit tests / integration tests)

### Standards / Domain Driven Design (DDD)

1. All HTTP calls to upstream services are made through the `AggregatorService`
    - The `AggregatorService` is located in the `aggregator` sub-module of this repository
    - You will see examples of how this is achieved in the `CaseOrchestration` service
    - Regardless of whether you are making a number of async calls, or a single synchronous one, the standard is to make all calls through the public function in the `AggregatorService`
        - you should not need to make any changes to the `AggregatorService` — hopefully it gives you everything you need
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

3. The `Orchestration layer` will be made up of `@Service` classes named `*OrchestrationService` and their job is to:
    - Inject the `AggregatorService`
    - Help orchestrate the async (and synchronous) calls using the `AggregatorService`
    - Return an `*OrchestrationDto` data class that holds `*Dto` responses from the upstream services
    - Hard rules:
        - Just get and return the data please!
        - No business-logic should sit in this layer
        - Do not unit test this layer (covered by integration tests)
    - The big plus here is that when we release a new version of the `AggregatorService` in the `aggregator` sub-module it will:
        - Only have rippling effects on the `Orchestration layer`
            - We will only need to refactor implementation code in the `Orchestration layer`
            - No unit test refactoring will be neccesary
            - The integration tests will give us coverage on the `AggregatorService` itself and need no refactoring either

4. The `Service` layer that sits in between the `*Controller` and `*OrchestrationService` can be unit tested in the usual way with the `mockk` library

5. We are practicing `Domain Driven Design` (a light version)
    - This is a big topic, of course, but here are a few examples of how we use it:
        - We are using root aggregates `*Aggregate` (e.g. `CaseAggregate`)
        - These root aggregates are hydrated in services
        - They are hydrated with data returned by the `Orchestration` layer
        - All business-logic / state should be handled by root aggregates

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
