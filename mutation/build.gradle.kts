dependencies {
  implementation(project(":common"))
  implementation(project(":infrastructure"))

  runtimeOnly("org.postgresql:postgresql")
  runtimeOnly("org.flywaydb:flyway-database-postgresql")
  implementation("org.flywaydb:flyway-core")

  testImplementation("io.mockk:mockk:1.14.6")
  testImplementation(testFixtures(project(":infrastructure")))
}