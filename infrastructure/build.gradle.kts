plugins {
  `java-test-fixtures`
  alias(libs.plugins.pact)
}

// Matches the conventions used by the hmpps-person-record provider (see its
// pact_provider_verification.yml / record_deployment.yml on add-pact-record-deployment-step):
// - broker auth via username/password secrets, not a bearer token
// - pacticipant version pinned to the commit SHA so it lines up with "deployed"/"mainBranch" selectors
// - branch recorded via GITHUB_BRANCH so provider verification can target a specific consumer branch
pact {
  publish {
    pactDirectory = "$buildDir/pacts"
    pactBrokerUrl = System.getenv("PACT_BROKER_URL") ?: "https://pact-broker-prod.apps.live-1.cloud-platform.service.justice.gov.uk"
    pactBrokerUsername = System.getenv("HMPPS_PACT_BROKER_USERNAME")
    pactBrokerPassword = System.getenv("HMPPS_PACT_BROKER_PASSWORD")
    consumerVersion = System.getenv("GITHUB_SHA") ?: "local"
    consumerBranch = System.getenv("GITHUB_BRANCH") ?: "local"
    tags = listOfNotNull(System.getenv("GITHUB_BRANCH"))
  }
}

dependencies {
  implementation(project(":common"))
  implementation(libs.hmpps.starter)
  implementation(libs.hmpps.sqs)
  implementation(libs.spring.cache)
  implementation(libs.redisson)
  implementation(libs.redisson.spring.cache)
  implementation(libs.spring.data.jpa)
  implementation(libs.coroutines.core)
  implementation(libs.shedlock.spring)
  implementation(libs.shedlock.jdbc)
  implementation(libs.javers)

  implementation(libs.sentry.spring.boot.starter.jakarta)

  testRuntimeOnly(libs.junit.platform.launcher)
  testImplementation(libs.hmpps.starter.test)
  testImplementation(libs.mockk)
  testImplementation(libs.pact.consumer.junit5)
  testImplementation(testFixtures(project(":infrastructure")))
  testImplementation(testFixtures(project(":common")))
  testFixturesImplementation(libs.hmpps.starter)
  testFixturesImplementation(libs.jackson.module.kotlin)
  testFixturesImplementation(project(":common"))
}
