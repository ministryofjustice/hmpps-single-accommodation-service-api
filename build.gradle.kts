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

tasks.register("ktlintFormatSubmodules") {
  group = "formatting"
  description = "Run ktlintFormat on all submodules after the root project"

  dependsOn(subprojects.map { it.tasks.named("ktlintFormat") })
  mustRunAfter(tasks.named("ktlintFormat"))
}

tasks.register("ktlintCheckSubmodules") {
  group = "linting"
  description = "Run ktlintCheck on all submodules after the root project"

  dependsOn(subprojects.map { it.tasks.named("ktlintCheck") })
  mustRunAfter(tasks.named("ktlintCheck"))
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
