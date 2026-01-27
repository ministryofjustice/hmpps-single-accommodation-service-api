dependencies {
  implementation(project(":common"))
  implementation(project(":infrastructure"))

  testImplementation("io.mockk:mockk:1.14.9")
  testImplementation(testFixtures(project(":infrastructure")))
}
