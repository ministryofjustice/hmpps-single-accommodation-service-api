dependencies {

  implementation(project(":common"))
  implementation(project(":infrastructure"))
  implementation(libs.hmpps.starter)
  implementation(libs.spring.data.jpa)
  implementation(libs.spring.json)
  implementation(libs.commonmark)
  implementation(libs.commonmark.tables)

  testImplementation(libs.hmpps.starter.test)
  testRuntimeOnly(libs.junit.platform.launcher)
  testImplementation(libs.mockk)
  testImplementation(testFixtures(project(":infrastructure")))
  testImplementation(testFixtures(project(":common")))
}

val refreshEligibilityGraphStatusCardPreview = tasks.register<Exec>("refreshEligibilityGraphStatusCardPreview") {
  group = "documentation"
  description = "Refresh the eligibility graph status card preview from the UI"
  val skipRefresh = providers.gradleProperty("skipStatusCardPreviewRefresh")
  onlyIf { !skipRefresh.isPresent }
  commandLine(
    rootProject.projectDir.resolve("scripts/update-status-card-preview.sh").absolutePath,
    "--preview-only",
    providers.gradleProperty("statusCardPreviewBranch").orElse("main").get(),
  )
}

tasks.named("processResources") {
  mustRunAfter(refreshEligibilityGraphStatusCardPreview)
}

rootProject.allprojects {
  tasks.configureEach {
    if (name in setOf("compileKotlin", "compileJava", "processResources")) {
      mustRunAfter(refreshEligibilityGraphStatusCardPreview)
    }
  }
}

tasks.register<JavaExec>("generateEligibilityRulesGraphs") {
  group = "documentation"
  description = "Refresh the UI status card preview and generate eligibility rules graphs"
  classpath = sourceSets.main.get().runtimeClasspath
  mainClass.set(
    "uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.graph.GenerateEligibilityRulesGraphKt",
  )
  args(
    rootProject.projectDir.resolve("docs/").absolutePath,
    rootProject.projectDir.resolve("docs/architecture/decisions/treeadrs").absolutePath,
    rootProject.projectDir.absolutePath,
  )
  dependsOn(refreshEligibilityGraphStatusCardPreview)
  dependsOn(tasks.named("classes"))
}
