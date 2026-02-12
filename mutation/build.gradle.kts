val shedlockVersion = "7.5.0"

dependencies {
  implementation(project(":common"))
  implementation(project(":infrastructure"))
  implementation("net.javacrumbs.shedlock:shedlock-spring:$shedlockVersion")
  implementation("net.javacrumbs.shedlock:shedlock-provider-jdbc-template:$shedlockVersion")
  runtimeOnly("org.postgresql:postgresql")
  runtimeOnly("org.flywaydb:flyway-database-postgresql")
  implementation("org.flywaydb:flyway-core")

  testImplementation("io.mockk:mockk:1.14.6")
  testImplementation(testFixtures(project(":common")))
  testImplementation(testFixtures(project(":infrastructure")))
}