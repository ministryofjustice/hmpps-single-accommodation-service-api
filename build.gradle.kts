plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "10.0.0"
  kotlin("plugin.spring") version "2.3.0"
  kotlin("plugin.jpa") version "2.3.0"
  id("io.gitlab.arturbosch.detekt") version "1.23.8"
}

detekt {
  config.setFrom("detekt/detekt.yml")
}

val hmppsSpringBootVersion = "2.0.0"
val hmppsSqsVersion = "6.0.1"
val springdocVersion = "3.0.1"
val wiremockVersion = "3.13.2"
val redissonVersion = "4.1.0"

val mockkVersion = "1.14.9"
val shedlockVersion = "7.5.0"

dependencies {
  implementation(project(":common"))
  implementation(project(":query"))
  implementation(project(":mutation"))

  implementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter:$hmppsSpringBootVersion")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-restclient")
  implementation("org.springframework.boot:spring-boot-starter-flyway")

  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:$springdocVersion")

  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")

  testImplementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter-test:$hmppsSpringBootVersion")
  testImplementation("uk.gov.justice.service.hmpps:hmpps-sqs-spring-boot-starter:$hmppsSqsVersion")
  testImplementation("org.springframework.boot:spring-boot-resttestclient")

  testImplementation("org.wiremock:wiremock-standalone:$wiremockVersion")
  testImplementation("io.mockk:mockk:$mockkVersion")
  testImplementation("io.swagger.parser.v3:swagger-parser:2.1.36") {
    exclude(group = "io.swagger.core.v3")
  }
  testImplementation("org.redisson:redisson-spring-boot-starter:$redissonVersion")

  testImplementation(testFixtures(project(":infrastructure")))
}

kotlin {
  jvmToolchain(25)
  compilerOptions {
    freeCompilerArgs.addAll("-Xannotation-default-target=param-property")
  }
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
  jvmTarget = "21"
}

configurations.matching { it.name == "detekt" }.all {
  resolutionStrategy.eachDependency {
    if (requested.group == "org.jetbrains.kotlin") {
      useVersion(io.gitlab.arturbosch.detekt.getSupportedKotlinVersion())
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

subprojects {
  repositories {
    mavenLocal()
    mavenCentral()
  }
  apply(plugin = "org.jetbrains.kotlin.jvm")
  apply(plugin = "org.jetbrains.kotlin.plugin.spring")
  apply(plugin = "org.jetbrains.kotlin.plugin.jpa")

  dependencies {
    implementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter:$hmppsSpringBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-json")

    testImplementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter-test:$hmppsSpringBootVersion")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
  }

  tasks.withType<Test> {
    useJUnitPlatform()
  }
}
