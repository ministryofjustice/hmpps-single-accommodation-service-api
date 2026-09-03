package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.graph

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

  val result = bootAndGenerate()
  writeGenerationResult(result, target)
}

fun bootAndGenerate(): GenerationResult {
  val context = AnnotationConfigApplicationContext()
  context.register(EligibilityRulesGraphConfiguration::class.java)
  context.refresh()
  try {
    val providers = context.getBeansOfType(EligibilityTreeProvider::class.java).values
    // val ruleSets = context.getBeansOfType(RuleSet::class.java).values
    return EligibilityRulesGraphGenerator.generate(providers)
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
