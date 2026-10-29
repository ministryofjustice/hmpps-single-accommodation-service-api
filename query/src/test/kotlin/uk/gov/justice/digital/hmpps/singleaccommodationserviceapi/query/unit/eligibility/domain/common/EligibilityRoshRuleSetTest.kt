package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.common

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.config.ClockConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.EligibilityRoshRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.RoshLevelRule

@ExtendWith(SpringExtension::class)
@ContextConfiguration(
  classes = [
    EligibilityRoshRuleSet::class,
    ClockConfig::class,
    RoshLevelRule::class,
  ],
)
class EligibilityRoshRuleSetTest {

  @Autowired
  lateinit var eligibilityRoshRuleSet: EligibilityRoshRuleSet

  private val expectedRuleNames = listOf(
    RoshLevelRule::class.java.simpleName,
  )

  @Test
  fun `all expected rules are included in EligibilityRoshRuleSet`() {
    val ruleSetRules = eligibilityRoshRuleSet.getRules().map { it.rule.javaClass.simpleName }

    Assertions.assertThat(ruleSetRules)
      .hasSize(1)
      .containsExactlyInAnyOrderElementsOf(expectedRuleNames)
  }
}
