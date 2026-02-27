import dev.detekt.gradle.plugin.getSupportedKotlinVersion

plugins {
  alias(libs.plugins.hmpps.spring.boot)
  alias(libs.plugins.kotlin.spring)
  alias(libs.plugins.kotlin.jpa)
  alias(libs.plugins.detekt)
}

detekt {
  config.setFrom("detekt/detekt.yml")
}

dependencies {
  implementation(project(":common"))
  implementation(project(":query"))
  implementation(project(":mutation"))

  implementation(libs.hmpps.starter)
  implementation(libs.spring.data.jpa)
  implementation(libs.spring.restclient)
  implementation(libs.spring.flyway)

  implementation(libs.springdoc)
  implementation("org.javers:javers-spring-boot-starter-sql:7.10.0")

  testImplementation(libs.hmpps.starter.test)
  testImplementation(libs.hmpps.sqs)
  testImplementation(libs.spring.resttestclient)

  testImplementation(libs.wiremock)
  testImplementation(libs.mockk)
  testImplementation(libs.swagger.parser) {
    exclude(group = "io.swagger.core.v3")
  }
  testImplementation(libs.redisson.boot)

  testImplementation(libs.awaitility)
  testImplementation(testFixtures(project(":infrastructure")))
}

kotlin {
  jvmToolchain(25)
  compilerOptions {
    freeCompilerArgs.addAll("-Xannotation-default-target=param-property")
  }
}

configurations.detekt {
  resolutionStrategy.eachDependency {
    if (requested.group == "org.jetbrains.kotlin") {
      useVersion(getSupportedKotlinVersion())
    }
  }
}

tasks.register<Copy>("copyPreCommitHook") {
  description = "Copy the pre-commit git hook from the scripts to the .git/hooks folder."
  group = "git hooks"
  outputs.upToDateWhen { false }
  from("$rootDir/scripts/pre-commit")
  into("$rootDir/.git/hooks/")
}

tasks.build {
  dependsOn("copyPreCommitHook")
}

allprojects {
  tasks.register<Test>("integrationTest") {
    group = "verification"
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath
    useJUnitPlatform {
      includeTags("integration")
    }
  }

  tasks.register<Test>("unitTest") {
    group = "verification"
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath
    useJUnitPlatform {
      excludeTags("integration")
    }
  }
}

tasks.named<Test>("unitTest") {
  finalizedBy("unitTestAggregateReport")
}

val unitTestAggregateReport by tasks.registering(TestReport::class) {
  group = "verification"
  description = "Aggregates unitTest results from root and subprojects"

  destinationDirectory.set(
    layout.buildDirectory.dir("reports/tests/unitTest"),
  )

  val allUnitTestTasks = allprojects.flatMap { project ->
    project.tasks.withType(Test::class)
      .matching { it.name == "unitTest" }
  }

  dependsOn(allUnitTestTasks)
  testResults.from(allUnitTestTasks.map { it.binaryResultsDirectory })
}

subprojects {
  repositories {
    mavenLocal()
    mavenCentral()
  }
  pluginManager.apply("dev.detekt")
  pluginManager.apply("org.jetbrains.kotlin.jvm")
  pluginManager.apply("org.jetbrains.kotlin.plugin.spring")
  pluginManager.apply("org.jetbrains.kotlin.plugin.jpa")
  pluginManager.apply("org.jlleitschuh.gradle.ktlint")

  detekt {
    config.setFrom("../detekt/detekt.yml")
  }

  tasks.withType<Test> {
    useJUnitPlatform()
  }
}
