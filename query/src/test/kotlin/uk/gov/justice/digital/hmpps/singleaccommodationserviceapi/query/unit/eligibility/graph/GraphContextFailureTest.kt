package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.graph

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.BeanCreationException
import org.springframework.beans.factory.NoSuchBeanDefinitionException
import org.springframework.beans.factory.UnsatisfiedDependencyException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.sentry.SentryService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.graph.formatGraphContextFailure

class GraphContextFailureTest {

  @Test
  fun `missing bean names the type, the depender, and how to fix it`() {
    val missing = NoSuchBeanDefinitionException(SentryService::class.java)
    val updaterFailure = UnsatisfiedDependencyException(
      null,
      "cas2SuitabilityContextUpdater",
      "sentryService",
      missing,
    )
    val providerFailure = BeanCreationException(
      "cas2EligibilityTreeProvider",
      "Error creating bean",
      updaterFailure,
    )

    val message = formatGraphContextFailure(providerFailure)
    println(message)

    assertThat(message).contains(SentryService::class.java.name)
    assertThat(message).contains("cas2SuitabilityContextUpdater")
    assertThat(message).contains("EligibilityRulesGraphConfiguration")
    assertThat(message).contains("sentryService()")
    assertThat(message).contains("./gradlew :query:generateEligibilityRulesGraph")
    assertThat(message).doesNotContain("Error creating bean")
  }

  @Test
  fun `other bean errors show the root cause and the same fix hint`() {
    val error = BeanCreationException("clock", "Error creating bean", IllegalStateException("clock exploded"))

    val message = formatGraphContextFailure(error)

    assertThat(message).contains("clock exploded")
    assertThat(message).contains("EligibilityRulesGraphConfiguration")
    assertThat(message).doesNotContain("missing bean")
  }
}
