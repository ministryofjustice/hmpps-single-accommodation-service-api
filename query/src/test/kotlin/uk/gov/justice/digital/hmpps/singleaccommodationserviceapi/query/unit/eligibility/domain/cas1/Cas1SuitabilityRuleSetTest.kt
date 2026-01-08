package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas1

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.config.ClockConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.Cas1SuitabilityRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.rules.ApplicationSuitabilityRule

@ExtendWith(SpringExtension::class)
@ContextConfiguration(
  classes = [
    Cas1SuitabilityRuleSet::class,
    ApplicationSuitabilityRule::class,
    ClockConfig::class,
  ],
)
class Cas1SuitabilityRuleSetTest {

  @Autowired
  lateinit var cas1SuitabilityRuleSet: Cas1SuitabilityRuleSet

  private val expectedCas1SuitabilityRuleNames = listOf(
    ApplicationSuitabilityRule::class.simpleName,
  )

  @Test
  fun `all Cas1SuitabilityRule components are included in Cas1SuitabilityRuleSet`() {
    val ruleSetRules = cas1SuitabilityRuleSet.getRules().map { it.javaClass.simpleName }

    assertThat(ruleSetRules)
      .hasSize(1)
      .containsExactlyInAnyOrderElementsOf(expectedCas1SuitabilityRuleNames)
  }
}
