package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas3.suitability

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.config.ClockConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.suitability.Cas3ApplicationPresentSuitabilityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.suitability.Cas3ApplicationSuitabilityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.suitability.Cas3AssessmentSuitabilityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.suitability.Cas3BookingSuitabilityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.suitability.Cas3SuitabilityRuleSet

@ExtendWith(SpringExtension::class)
@ContextConfiguration(
  classes = [
    Cas3SuitabilityRuleSet::class,
    Cas3ApplicationPresentSuitabilityRule::class,
    Cas3BookingSuitabilityRule::class,
    Cas3ApplicationSuitabilityRule::class,
    Cas3AssessmentSuitabilityRule::class,
    ClockConfig::class,
  ],
)
class Cas3SuitabilityRuleSetTest {

  @Autowired
  lateinit var cas3SuitabilityRuleSet: Cas3SuitabilityRuleSet

  private val expectedCas3SuitabilityRuleNames = listOf(
    Cas3ApplicationPresentSuitabilityRule::class.simpleName,
    Cas3BookingSuitabilityRule::class.simpleName,
    Cas3ApplicationSuitabilityRule::class.simpleName,
    Cas3AssessmentSuitabilityRule::class.simpleName,
  )

  @Test
  fun `all Cas3SuitabilityRule components are included in Cas3SuitabilityRuleSet`() {
    val ruleSetRules = cas3SuitabilityRuleSet.getRules().map { it.javaClass.simpleName }

    assertThat(ruleSetRules)
      .hasSize(4)
      .containsExactlyInAnyOrderElementsOf(expectedCas3SuitabilityRuleNames)
  }
}
