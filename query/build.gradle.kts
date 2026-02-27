dependencies {

  implementation(project(":common"))
  implementation(project(":infrastructure"))
  implementation(libs.hmpps.starter)
  implementation(libs.spring.data.jpa)
  implementation("org.javers:javers-spring-boot-starter-sql:7.10.0")

  testImplementation(libs.hmpps.starter.test)
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
  testImplementation(libs.mockk)
  testImplementation(testFixtures(project(":infrastructure")))
}
