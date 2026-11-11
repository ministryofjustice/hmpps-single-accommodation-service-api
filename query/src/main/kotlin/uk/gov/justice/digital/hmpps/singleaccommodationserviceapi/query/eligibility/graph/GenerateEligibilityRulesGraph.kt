package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.graph

import org.springframework.beans.BeansException
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.EligibilityTreeProvider
import java.nio.file.Files
import java.nio.file.Path
import kotlin.system.exitProcess

fun main(args: Array<String>) {
  if (args.isEmpty()) {
    System.err.println("Usage: generateEligibilityRulesGraph <output-folder>")
    exitProcess(1)
  }
  val outputFolder = Path.of(args[0])

  try {
    bootAndGenerate(
      renderPaths = listOf(
        RenderPath(
          path = outputFolder.resolve("eligibility-rules-graph.md"),
          renderer = RulesGraphMarkdownRenderer,
        ),
        RenderPath(
          path = outputFolder.resolve("eligibility-rules-graph.html"),
          renderer = RulesGraphHtmlRenderer,
        ),
      ),
    )
  } catch (ex: GraphContextStartupException) {
    System.err.println(ex.message)
    exitProcess(1)
  }
}

fun bootAndGenerate(
  renderPaths: List<RenderPath>,
) {
  val context = AnnotationConfigApplicationContext()
  context.environment.setActiveProfiles(ELIGIBILITY_GRAPH_PROFILE)
  context.register(EligibilityRulesGraphConfiguration::class.java)
  try {
    context.refresh()
    val providers = context.getBeansOfType(EligibilityTreeProvider::class.java).values

    renderPaths.forEach {
      val result = EligibilityRulesGraphGenerator.generate(providers, it.renderer)
      writeGenerationResult(result, it.path)
    }
  } catch (ex: BeansException) {
    throw GraphContextStartupException(formatGraphContextFailure(ex), ex)
  } finally {
    context.close()
  }
}

fun writeGenerationResult(result: GenerationResult, target: Path) {
  val tmp = target.resolveSibling("${target.fileName}")
  target.parent?.let { Files.createDirectories(it) }
  Files.writeString(tmp, result.text)
  println("Wrote $target")
}

data class RenderPath(
  val renderer: RulesGraphRenderer,
  val path: Path,
)
