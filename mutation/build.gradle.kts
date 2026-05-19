dependencies {
  implementation(project(":common"))
  implementation(project(":infrastructure"))
  implementation(libs.hmpps.starter)
  implementation(libs.spring.data.jpa)
  implementation(libs.spring.json)
  implementation(libs.coroutines.core)
  implementation(libs.hmpps.sqs)
  implementation(libs.shedlock.spring)
  implementation(libs.shedlock.jdbc)

  runtimeOnly(libs.postgres)
  runtimeOnly(libs.flyway.core)
  implementation(libs.flyway.postgres)

  testRuntimeOnly(libs.junit.platform.launcher)
  testImplementation(libs.hmpps.starter.test)
  testImplementation(libs.mockk)
  testImplementation(testFixtures(project(":common")))
  testImplementation(testFixtures(project(":infrastructure")))
}
