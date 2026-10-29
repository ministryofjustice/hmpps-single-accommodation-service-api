package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas1.upcoming

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.config.ClockConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.upcoming.ReleaseWithinTwentyEightWeeksRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.upcoming.rules.ReleaseOutsideTwentyEightWeeksRule

@ExtendWith(SpringExtension::class)
@ContextConfiguration(
  classes = [
    ReleaseOutsideTwentyEightWeeksRule::class,
    ReleaseWithinTwentyEightWeeksRuleSet::class,
    ClockConfig::class,
  ],
)
class ReleaseWithinTwentyEightWeeksRuleSetTest {

  @Autowired
  lateinit var ruleSet: ReleaseWithinTwentyEightWeeksRuleSet

  private val expectedRuleNames = listOf(
    ReleaseOutsideTwentyEightWeeksRule::class.java.simpleName,
  )

  @Test
  fun `all rule components are included in rule set`() {
    val ruleSetRules = ruleSet.getRules().map { it.rule.javaClass.simpleName }

    assertThat(ruleSetRules)
      .hasSize(1)
      .containsExactlyInAnyOrderElementsOf(expectedRuleNames)
  }
}
