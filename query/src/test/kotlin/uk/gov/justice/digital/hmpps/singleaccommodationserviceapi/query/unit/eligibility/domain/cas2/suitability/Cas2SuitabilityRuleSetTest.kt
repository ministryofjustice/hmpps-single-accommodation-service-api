package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas2.suitability

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.config.ClockConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.suitability.Cas2ApplicationSubmittedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.suitability.Cas2SuitabilityRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.suitability.Cas2SuitableStatusRule

@ExtendWith(SpringExtension::class)
@ContextConfiguration(
  classes = [
    Cas2SuitabilityRuleSet::class,
    Cas2ApplicationSubmittedRule::class,
    Cas2SuitableStatusRule::class,
    ClockConfig::class,
  ],
)
class Cas2SuitabilityRuleSetTest {

  @Autowired
  lateinit var cas2SuitabilityRuleSet: Cas2SuitabilityRuleSet

  private val expectedCas2SuitabilityRuleNames = listOf(
    Cas2ApplicationSubmittedRule::class.simpleName,
    Cas2SuitableStatusRule::class.simpleName,
  )

  @Test
  fun `all Cas2SuitabilityRule components are included in Cas2SuitabilityRuleSet`() {
    val ruleSetRules = cas2SuitabilityRuleSet.getRules().map { it.javaClass.simpleName }

    assertThat(ruleSetRules)
      .hasSize(2)
      .containsExactlyInAnyOrderElementsOf(expectedCas2SuitabilityRuleNames)
  }
}
