package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.common

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.config.ClockConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.ReleaseWithin15DaysRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.rules.ReleaseOutside15DaysRule

@ExtendWith(SpringExtension::class)
@ContextConfiguration(
  classes = [
    ReleaseOutside15DaysRule::class,
    ReleaseWithin15DaysRuleSet::class,
    ClockConfig::class,
  ],
)
class ReleaseWithin15DaysRuleSetTest {

  @Autowired
  lateinit var ruleSet: ReleaseWithin15DaysRuleSet

  private val expectedRuleNames = listOf(
    ReleaseOutside15DaysRule::class.java.simpleName,
  )

  @Test
  fun `all rule components are included in rule set`() {
    val ruleSetRules = ruleSet.getRules().map { it.rule.javaClass.simpleName }

    assertThat(ruleSetRules)
      .hasSize(1)
      .containsExactlyInAnyOrderElementsOf(expectedRuleNames)
  }
}
