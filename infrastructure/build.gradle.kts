plugins {
  `java-test-fixtures`
}

dependencies {
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-cache")
  implementation("org.redisson:redisson:4.1.0")
  implementation("org.redisson:redisson-spring-cache:4.1.0")
}
