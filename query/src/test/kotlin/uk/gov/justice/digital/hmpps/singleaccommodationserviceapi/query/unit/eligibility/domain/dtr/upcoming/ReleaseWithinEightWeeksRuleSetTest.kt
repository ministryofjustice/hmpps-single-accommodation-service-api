package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.dtr.upcoming

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.config.ClockConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.upcoming.ReleaseWithinEightWeeksRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.upcoming.rules.ReleaseOutsideEightWeeksRule

@ExtendWith(SpringExtension::class)
@ContextConfiguration(
  classes = [
    ReleaseOutsideEightWeeksRule::class,
    ReleaseWithinEightWeeksRuleSet::class,
    ClockConfig::class,
  ],
)
class ReleaseWithinEightWeeksRuleSetTest {

  @Autowired
  lateinit var ruleSet: ReleaseWithinEightWeeksRuleSet

  private val expectedRuleNames = listOf(
    ReleaseOutsideEightWeeksRule::class.java.simpleName,
  )

  @Test
  fun `all rule components are included in rule set`() {
    val ruleSetRules = ruleSet.getRules().map { it.rule.javaClass.simpleName }

    assertThat(ruleSetRules)
      .hasSize(1)
      .containsExactlyInAnyOrderElementsOf(expectedRuleNames)
  }
}
