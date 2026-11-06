import dev.detekt.gradle.plugin.getSupportedKotlinVersion
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket

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
  implementation(project(":infrastructure"))
  implementation(project(":query"))
  implementation(project(":mutation"))

  implementation(libs.hmpps.starter)
  implementation(libs.spring.data.jpa)
  implementation(libs.spring.webclient)
  implementation(libs.spring.flyway)

  implementation(libs.springdoc)
  implementation(libs.javers)

  // Due to use of a spring bom in hmpps-starter we have to force some versions to override them locally
  implementation(enforcedPlatform(libs.postgres))

  testImplementation(libs.hmpps.starter.test)
  testImplementation(libs.hmpps.sqs)
  testImplementation(libs.coroutines.core)

  testImplementation(libs.wiremock)
  testImplementation(libs.mockk)
  testImplementation(libs.swagger.parser) {
    exclude(group = "io.swagger.core.v3")
  }
  testImplementation(libs.redisson.boot)

  testImplementation(libs.awaitility)
  testImplementation(libs.webtestclient)
  testImplementation(testFixtures(project(":infrastructure")))
  testImplementation(testFixtures(project(":common")))
  testImplementation(libs.sartestsupport)
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

tasks.register<Copy>("copyGitHooks") {
  description = "Copy git hooks."
  group = "git hooks"
  from("$rootDir/scripts/hooks/pre-push")
  into("$rootDir/.git/hooks/")

  doLast {
    file("$rootDir/.git/hooks/pre-push").setExecutable(true)
  }
}

tasks.register<Exec>("validateFlywayFilenames") {
  description = "Validate Flyway migration filenames"
  group = "verification"
  commandLine("sh", "$rootDir/scripts/validate_flyway_filenames.sh")
}

tasks.compileKotlin {
  dependsOn("copyGitHooks")
}

tasks.named("check") {
  dependsOn("validateFlywayFilenames")
}

tasks.register<Exec>("ensureTestDependencies") {
  group = "sar"
  description = "Runs 'docker compose up' if the integration test database is not already accepting connections"

  commandLine("docker", "compose", "up", "-d", "--wait")
  onlyIf {
    val running = try {
      Socket().use { it.connect(InetSocketAddress("localhost", 5434), 1000) }
      true
    } catch (e: IOException) {
      false
    }
    !running
  }
}

fun Test.configureSarTest(vararg testPatterns: String) {
  dependsOn("ensureTestDependencies")
  group = "sar"
  testClassesDirs = sourceSets.test.get().output.classesDirs
  classpath = sourceSets.test.get().runtimeClasspath
  useJUnitPlatform()
  filter {
    testPatterns.forEach { includeTestsMatching(it) }
  }
  outputs.upToDateWhen { false }
}

fun Test.copyGeneratedSarFiles(generatedFiles: () -> Map<File, File>) {
  doFirst {
    // Avoid copying stale files from a previous run
    generatedFiles().keys.forEach { it.delete() }
  }
  doLast {
    generatedFiles().filterKeys { it.exists() }.forEach { (generated, target) ->
      generated.copyTo(target, overwrite = true)
      generated.delete()
      println("Updated ${target.relativeTo(projectDir)}")
    }
  }
}

val sarTestResourcesDir = file("src/test/resources")
val sarFixturesDir = file("src/test/resources/sar")

tasks.register<Test>("generateSarPdf") {
  configureSarTest("*SasSarPdfReportTest")
  description = "Renders the SAR report as a PDF to build/test-generated/sar-generated-report.pdf"
}

tasks.register<Test>("updateSarSchemaTrackers") {
  configureSarTest("*SarIntegrationTest*JpaEntitiesTest*")
  description = "Updates expected-flyway-schema-version in application-test-sar.yml and regenerates sar/sas-entities-schema.json"
  environment("SAR_GENERATE_ACTUAL", "true")

  val migrationsDir = file("infrastructure/src/main/resources/db/migration")
  val sarConfig = sarTestResourcesDir.resolve("application-test-sar.yml")
  val migrationVersion = Regex("""^V(\d+)__.*\.sql$""")

  doFirst {
    val latestVersion = migrationsDir.listFiles()!!
      .mapNotNull { migrationVersion.find(it.name)?.groupValues?.get(1) }
      .maxBy { it.toLong() }
    sarConfig.writeText(
      sarConfig.readText().replace(
        Regex("""(expected-flyway-schema-version:\s*)"?\d+"?"""),
        "$1\"$latestVersion\"",
      ),
    )
    println("Set expected-flyway-schema-version to $latestVersion")
  }

  copyGeneratedSarFiles {
    mapOf(sarTestResourcesDir.resolve("entity-schema.json.log") to sarFixturesDir.resolve("sas-entities-schema.json"))
  }
}

tasks.register<Test>("updateSarFixtures") {
  configureSarTest("*SasSarComplianceTest", "*SasSarPdfReportTest")
  description = "Regenerates the expected SAR API response, HTML report and render result fixtures in src/test/resources/sar"
  environment("SAR_GENERATE_ACTUAL", "true")

  copyGeneratedSarFiles {
    listOf("sas-expected-api-response.json", "sas-expected-report.html", "sas-expected-render-result.html")
      .associate { sarTestResourcesDir.resolve("$it.log") to sarFixturesDir.resolve(it) }
  }
}

val guavaDependency = libs.guava

allprojects {
  pluginManager.withPlugin("java") {
    dependencies {
      constraints {
        add("implementation", guavaDependency.get()) {
          because("Fix CVE-2026-102554")
        }
      }
    }
  }

  pluginManager.apply("org.owasp.dependencycheck")
  dependencyCheck {
    skipConfigurations.addAll(listOf("detekt", "detektPlugins"))
  }

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
    filter {
      excludeTestsMatching("*PactTest")
    }
  }

  if (this != rootProject) {
    tasks.register<Test>("pactTest") {
      group = "verification"
      description = "Runs consumer-side Pact tests"
      testClassesDirs = sourceSets.test.get().output.classesDirs
      classpath = sourceSets.test.get().runtimeClasspath
      useJUnitPlatform()
      filter {
        includeTestsMatching("*PactTest")
        isFailOnNoMatchingTests = false
      }
    }
  }
}

tasks.register("pactTest") {
  group = "verification"
  description = "Runs all consumer-side Pact tests"
  dependsOn(subprojects.map { it.tasks.named("pactTest") })
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
