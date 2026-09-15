package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.graph

import org.springframework.beans.BeansException
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EligibilityTreeProvider
import java.nio.file.Files
import java.nio.file.Path
import kotlin.system.exitProcess

fun main(args: Array<String>) {
  if (args.isEmpty()) {
    System.err.println("Usage: generateEligibilityRulesGraph <output-markdown-path>")
    exitProcess(1)
  }
  val target = Path.of(args[0])

  try {
    val result = bootAndGenerate()
    writeGenerationResult(result, target)
  } catch (ex: GraphContextStartupException) {
    System.err.println(ex.message)
    exitProcess(1)
  }
}

fun bootAndGenerate(): GenerationResult {
  val context = AnnotationConfigApplicationContext()
  context.environment.setActiveProfiles(ELIGIBILITY_GRAPH_PROFILE)
  context.register(EligibilityRulesGraphConfiguration::class.java)
  try {
    context.refresh()
    val providers = context.getBeansOfType(EligibilityTreeProvider::class.java).values
    return EligibilityRulesGraphGenerator.generate(providers)
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
