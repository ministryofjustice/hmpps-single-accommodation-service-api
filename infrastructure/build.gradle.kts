plugins {
  `java-test-fixtures`
}
val hmppsSqsVersion = "6.0.1"
val shedlockVersion = "7.5.0"

dependencies {
  implementation("org.springframework.boot:spring-boot-starter-cache")
  implementation("org.redisson:redisson:4.1.0")
  implementation("org.redisson:redisson-spring-cache:4.1.0")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
  implementation("uk.gov.justice.service.hmpps:hmpps-sqs-spring-boot-starter:$hmppsSqsVersion")
  implementation("net.javacrumbs.shedlock:shedlock-spring:$shedlockVersion")
  implementation("net.javacrumbs.shedlock:shedlock-provider-jdbc-template:$shedlockVersion")
}
