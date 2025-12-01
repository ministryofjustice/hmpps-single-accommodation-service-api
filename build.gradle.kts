import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "9.2.0"
  kotlin("plugin.spring") version "2.2.21"
}

configurations {
  testImplementation { exclude(group = "org.junit.vintage") }
}

dependencies {
  implementation(project(":aggregator"))

  implementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter:1.8.2")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.14")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
  implementation("org.springframework.boot:spring-boot-starter-cache")
  implementation("org.redisson:redisson-spring-boot-starter:3.27.2")

  testImplementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter-test:1.8.2")
  testImplementation("org.wiremock:wiremock-standalone:3.13.2")
  testImplementation("io.mockk:mockk:1.14.6")
  testImplementation("io.swagger.parser.v3:swagger-parser:2.1.36") {
    exclude(group = "io.swagger.core.v3")
  }
}

kotlin {
  jvmToolchain(21)
}

tasks {
  withType<KotlinCompile> {
    compilerOptions.jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
  }
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.compilerOptions {
  freeCompilerArgs.set(
    listOf(
      "-Xannotation-default-target=param-property",
    ),
  )
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
