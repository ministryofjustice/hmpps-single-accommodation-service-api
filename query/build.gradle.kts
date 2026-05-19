dependencies {

  implementation(project(":common"))
  implementation(project(":infrastructure"))
  implementation(libs.hmpps.starter)
  implementation(libs.spring.data.jpa)

  testImplementation(libs.hmpps.starter.test)
  testRuntimeOnly(libs.junit.platform.launcher)
  testImplementation(libs.mockk)
  testImplementation(testFixtures(project(":infrastructure")))
  testImplementation(testFixtures(project(":common")))
  testImplementation(libs.apache.commons.csv)
}
