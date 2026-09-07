package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas2.eligibility

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.config.ClockConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligibility.Cas2EligibilityRuleSet

@ExtendWith(SpringExtension::class)
@ContextConfiguration(
  classes = [
    Cas2EligibilityRuleSet::class,
    ClockConfig::class,
  ],
)
class Cas2EligibilityRuleSetTest {

  @Autowired
  lateinit var cas2EligibilityRuleSet: Cas2EligibilityRuleSet

  private val expectedCas2EligibilityRuleNames = emptyList<String>()

  @Test
  fun `all Cas2EligibilityRule components are included in Cas2EligibilityRuleSet`() {
    val ruleSetRules = cas2EligibilityRuleSet.getRules().map { it.javaClass.simpleName }

    assertThat(ruleSetRules)
      .hasSize(0)
      .containsExactlyInAnyOrderElementsOf(expectedCas2EligibilityRuleNames)
  }
}
