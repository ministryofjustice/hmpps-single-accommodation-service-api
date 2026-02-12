dependencies {
  implementation(project(":common"))
  implementation(project(":infrastructure"))
  implementation(libs.hmpps.starter)
  implementation(libs.spring.data.jpa)
  implementation(libs.spring.json)

  runtimeOnly(libs.postgres)
  runtimeOnly(libs.flyway.postgres)
  implementation(libs.flyway.core)

  testRuntimeOnly("org.junit.platform:junit-platform-launcher")

  testImplementation(libs.hmpps.starter.test)
  testImplementation(libs.mockk)
  testImplementation(testFixtures(project(":common")))
  testImplementation(testFixtures(project(":infrastructure")))
}
