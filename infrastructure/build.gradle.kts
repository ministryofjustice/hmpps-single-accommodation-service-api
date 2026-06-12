plugins {
  `java-test-fixtures`
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
  implementation(libs.app.insights.core)
  implementation(libs.micrometer.registry.azure)

  implementation(libs.sentry.spring.boot.starter.jakarta)

  testRuntimeOnly(libs.junit.platform.launcher)
  testImplementation(libs.hmpps.starter.test)
  testImplementation(libs.mockk)
  testImplementation(testFixtures(project(":infrastructure")))
}
