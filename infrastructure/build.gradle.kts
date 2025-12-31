plugins {
  `java-test-fixtures`
}

dependencies {
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-cache")
  implementation("org.redisson:redisson-spring-boot-starter:3.27.2")
}