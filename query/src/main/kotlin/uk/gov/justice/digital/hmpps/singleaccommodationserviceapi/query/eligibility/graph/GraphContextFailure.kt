package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.graph

import org.springframework.beans.factory.BeanCreationException
import org.springframework.beans.factory.NoSuchBeanDefinitionException
import org.springframework.beans.factory.UnsatisfiedDependencyException

class GraphContextStartupException(message: String, cause: Throwable) : RuntimeException(message, cause)

fun formatGraphContextFailure(error: Throwable): String {
  val missing = error.findCause<NoSuchBeanDefinitionException>()
  if (missing != null) {
    return formatMissingBean(error, missing)
  }

  val root = generateSequence(error) { it.cause }.last()
  return buildString {
    appendLine("Could not start the eligibility rules graph generator.")
    appendLine()
    appendLine("${root::class.java.simpleName}: ${root.message}")
    appendLine()
    append(FIX_HINT)
  }
}

private fun formatMissingBean(error: Throwable, missing: NoSuchBeanDefinitionException): String {
  val missingType = missing.beanType?.typeName ?: missing.beanName ?: "unknown"
  val requiredBy = error.findCause<UnsatisfiedDependencyException>()?.beanName
    ?: error.findCause<BeanCreationException>()?.beanName

  return buildString {
    appendLine("Could not start the eligibility rules graph generator.")
    appendLine()
    appendLine("A scanned eligibility component needs a Spring bean that this standalone task does not provide:")
    appendLine()
    appendLine("  missing bean : $missingType")
    if (requiredBy != null) {
      appendLine("  required by  : $requiredBy")
    }
    appendLine()
    append(FIX_HINT)
  }
}

private val FIX_HINT = """
  This task only loads EligibilityRulesGraphConfiguration, not the full application.

  Add a no-op @Bean for the missing type in EligibilityRulesGraphConfiguration
  (same pattern as sentryService()), then re-run:

    ./gradlew :query:generateEligibilityRulesGraph
""".trimIndent()

private inline fun <reified T : Throwable> Throwable.findCause(): T? = generateSequence(this) { it.cause }
  .filterIsInstance<T>()
  .lastOrNull()
