plugins {
  `java-test-fixtures`
}

dependencies {
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-cache")
  implementation("org.redisson:redisson-spring-boot-starter:3.27.2")
  implementation("uk.gov.justice.service.hmpps:hmpps-sqs-spring-boot-starter:5.4.11")
  implementation("net.javacrumbs.shedlock:shedlock-spring:7.5.0")
  implementation("net.javacrumbs.shedlock:shedlock-provider-jdbc-template:7.5.0")
}