package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.graph

import org.springframework.context.annotation.AnnotationConfigApplicationContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EligibilityTreeProvider

fun main(args: Array<String>) {
  val result = bootAndGenerate()
  println(result.text)
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
