plugins {
  `java-test-fixtures`
}

dependencies {
  implementation(libs.hmpps.starter)
  implementation(libs.spring.json)
  testFixturesImplementation(libs.apache.commons.csv)
}
