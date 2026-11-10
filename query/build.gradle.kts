dependencies {

  implementation(project(":common"))
  implementation(project(":infrastructure"))
  implementation(libs.hmpps.starter)
  implementation(libs.spring.data.jpa)
  implementation(libs.spring.json)

  testImplementation(libs.hmpps.starter.test)
  testRuntimeOnly(libs.junit.platform.launcher)
  testImplementation(libs.mockk)
  testImplementation(testFixtures(project(":infrastructure")))
  testImplementation(testFixtures(project(":common")))
}

tasks.register<JavaExec>("generateEligibilityRulesGraphs") {
  group = "documentation"
  description = "Generate eligibility rules graphs from code"
  classpath = sourceSets.main.get().runtimeClasspath
  mainClass.set(
    "uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.graph.GenerateEligibilityRulesGraphKt",
  )
  args(rootProject.projectDir.resolve("docs/").absolutePath)
  dependsOn(tasks.named("classes"))
}
