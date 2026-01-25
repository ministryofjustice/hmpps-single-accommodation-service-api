dependencies {
  implementation(project(":common"))
  implementation(project(":infrastructure"))

  testImplementation("io.mockk:mockk:1.14.6")
  testImplementation(testFixtures(project(":infrastructure")))
}