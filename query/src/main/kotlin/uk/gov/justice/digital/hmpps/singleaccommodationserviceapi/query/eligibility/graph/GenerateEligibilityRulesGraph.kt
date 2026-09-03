package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.graph

import org.springframework.context.annotation.AnnotationConfigApplicationContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EligibilityTreeProvider
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet

fun main(args: Array<String>) {
  boot()
}

fun boot() {
  val context = AnnotationConfigApplicationContext()
  context.register(EligibilityRulesGraphConfiguration::class.java)
  context.refresh()
  try {
    val providers = context.getBeansOfType(EligibilityTreeProvider::class.java).values
    val ruleSets = context.getBeansOfType(RuleSet::class.java).values

    println("Found ${providers.size} providers")
    providers.forEach { provider ->
      println(provider.javaClass.name)
    }

    println("Found ${ruleSets.size} rule sets")
    ruleSets.forEach { ruleSet ->
      println(ruleSet.javaClass.name)
    }
  } finally {
    context.close()
  }
}
