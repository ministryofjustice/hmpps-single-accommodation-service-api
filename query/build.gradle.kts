dependencies {
  implementation(project(":common"))
  implementation(project(":infrastructure"))
  implementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter:1.8.2")

  testImplementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter-test:1.8.2")
  testImplementation("io.mockk:mockk:1.14.6")

  testImplementation(testFixtures(project(":infrastructure")))
}