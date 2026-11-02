package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.graph

import java.nio.file.Path
import kotlin.system.exitProcess

fun main(args: Array<String>) {
  if (args.isEmpty()) {
    System.err.println("Usage: generateEligibilityRulesGraphHtml <output-html-path>")
    exitProcess(1)
  }
  val target = Path.of(args[0])

  try {
    val result = bootAndGenerate(RulesGraphHtmlRenderer::render)
    writeGenerationResult(result, target)
  } catch (ex: GraphContextStartupException) {
    System.err.println(ex.message)
    exitProcess(1)
  }
}
