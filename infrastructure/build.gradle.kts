plugins {
  `java-test-fixtures`
}
dependencies {
  implementation(libs.hmpps.starter)
  implementation(libs.hmpps.sqs)
  implementation(libs.spring.cache)
  implementation(libs.redisson)
  implementation(libs.redisson.spring.cache)
  implementation(libs.spring.data.jpa)
  implementation(libs.coroutines.core)
  implementation(libs.shedlock.spring)
  implementation(libs.shedlock.jdbc)
}
