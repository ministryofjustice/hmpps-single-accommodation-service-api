package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.graph

import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDate
import java.util.Locale
import kotlin.io.path.name

fun loadScenarioChangeDates(repository: Path, treeNames: Set<String>): Map<String, LocalDate> {
  if (treeNames.isEmpty()) return emptyMap()
  if (git(repository, "rev-parse", "--is-shallow-repository") == "true") {
    git(repository, "fetch", "--unshallow", "--no-tags", "origin")
  }
  require(git(repository, "rev-parse", "--is-shallow-repository") == "false") {
    "Full Git history is required to determine tree ADR freshness; origin is also shallow"
  }
  val resources = repository.resolve("query/src/test/resources")
  require(Files.isDirectory(resources)) { "Scenario directory does not exist: $resources" }
  val scenarioFiles = Files.list(resources).use { paths ->
    paths.filter { Files.isRegularFile(it) }.map { it.name }.toList()
  }
  return treeNames.mapNotNull { tree ->
    val prefix = tree.lowercase(Locale.ROOT)
    val files = scenarioFiles.filter { it == "$prefix-eligibility-scenarios.csv" || it == "$prefix-application-scenarios.csv" }
      .sorted()
    if (files.isEmpty()) return@mapNotNull null
    val dates = files.map { file ->
      val path = "query/src/test/resources/$file"
      val date = git(repository, "log", "-1", "--format=%cs", "--", path)
      require(date.isNotEmpty()) { "No Git history for scenario file: $path" }
      LocalDate.parse(date)
    }
    tree to dates.max()
  }.toMap()
}

private fun git(repository: Path, vararg arguments: String): String {
  val command = ProcessBuilder(listOf("git", "-C", repository.toString()) + arguments).redirectErrorStream(true)
  command.environment()["GIT_TERMINAL_PROMPT"] = "0"
  val process = command.start()
  val output = process.inputStream.bufferedReader().use { it.readText() }.trim()
  check(process.waitFor() == 0) { "Git ${arguments.joinToString(" ")} failed: $output" }
  return output
}
