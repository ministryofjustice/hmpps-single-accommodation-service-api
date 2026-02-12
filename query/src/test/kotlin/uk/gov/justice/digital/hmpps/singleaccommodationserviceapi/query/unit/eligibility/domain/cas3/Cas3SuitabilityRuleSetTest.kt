package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas3

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.config.ClockConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.Cas3SuitabilityRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.rules.Cas3ApplicationSuitabilityRule

@ExtendWith(SpringExtension::class)
@ContextConfiguration(
  classes = [
    Cas3SuitabilityRuleSet::class,
    Cas3ApplicationSuitabilityRule::class,
    ClockConfig::class,
  ],
)
class Cas3SuitabilityRuleSetTest {

  @Autowired
  lateinit var cas3SuitabilityRuleSet: Cas3SuitabilityRuleSet

  private val expectedCas3SuitabilityRuleNames = listOf(
    Cas3ApplicationSuitabilityRule::class.simpleName,
  )

  @Test
  fun `all Cas3SuitabilityRule components are included in Cas3SuitabilityRuleSet`() {
    val ruleSetRules = cas3SuitabilityRuleSet.getRules().map { it.javaClass.simpleName }

    assertThat(ruleSetRules)
      .hasSize(1)
      .containsExactlyInAnyOrderElementsOf(expectedCas3SuitabilityRuleNames)
  }
}
