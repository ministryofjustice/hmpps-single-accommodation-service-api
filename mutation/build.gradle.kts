val hmppsSqsVersion = "6.0.1"
val shedlockVersion = "7.5.0"

dependencies {
  implementation(project(":common"))
  implementation(project(":infrastructure"))
  implementation(libs.hmpps.starter)
  implementation(libs.spring.data.jpa)
  implementation(libs.spring.json)

  runtimeOnly(libs.postgres)
  runtimeOnly(libs.flyway.postgres)
  implementation(libs.flyway.core)
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-cache")
  implementation("org.redisson:redisson:4.1.0")
  implementation("org.redisson:redisson-spring-cache:4.1.0")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
  implementation("uk.gov.justice.service.hmpps:hmpps-sqs-spring-boot-starter:$hmppsSqsVersion")
  implementation("net.javacrumbs.shedlock:shedlock-spring:$shedlockVersion")
  implementation("net.javacrumbs.shedlock:shedlock-provider-jdbc-template:$shedlockVersion")
  runtimeOnly("org.postgresql:postgresql")
  runtimeOnly("org.flywaydb:flyway-database-postgresql")
  implementation("org.flywaydb:flyway-core")

  testRuntimeOnly("org.junit.platform:junit-platform-launcher")

  testImplementation(libs.hmpps.starter.test)
  testImplementation(libs.mockk)
  testImplementation(testFixtures(project(":common")))
  testImplementation(testFixtures(project(":infrastructure")))
}
