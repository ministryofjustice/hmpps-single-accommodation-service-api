import dev.detekt.gradle.plugin.getSupportedKotlinVersion

plugins {
  alias(libs.plugins.hmpps.spring.boot)
  alias(libs.plugins.kotlin.spring)
  alias(libs.plugins.kotlin.jpa)
  alias(libs.plugins.detekt)
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

tasks.register<Test>("integrationTest") {
  group = "verification"
  description = "Runs integration tests (*IT)"

  useJUnitPlatform()

  testClassesDirs = sourceSets.test.get().output.classesDirs
  classpath = sourceSets.test.get().runtimeClasspath

  include("**/*IT.class")
}

tasks.named<Test>("test") {
  include("**/*Test.class")
}

allprojects {
  pluginManager.apply("org.jlleitschuh.gradle.ktlint")
  pluginManager.apply("dev.detekt")
  detekt {
    config.setFrom("$rootDir/detekt/detekt.yml")
  }
}

subprojects {
  repositories {
    mavenLocal()
    mavenCentral()
  }

  pluginManager.apply("org.jetbrains.kotlin.jvm")
  pluginManager.apply("org.jetbrains.kotlin.plugin.spring")
  pluginManager.apply("org.jetbrains.kotlin.plugin.jpa")

  tasks.withType<Test> {
    useJUnitPlatform()
  }
}
