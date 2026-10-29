package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas1.upcoming

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.config.ClockConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.upcoming.ReleaseWithin28WeeksRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.upcoming.rules.ReleaseOutside28WeeksRule

@ExtendWith(SpringExtension::class)
@ContextConfiguration(
  classes = [
    ReleaseOutside28WeeksRule::class,
    ReleaseWithin28WeeksRuleSet::class,
    ClockConfig::class,
  ],
)
class ReleaseWithin28WeeksRuleSetTest {

  @Autowired
  lateinit var ruleSet: ReleaseWithin28WeeksRuleSet

  private val expectedRuleNames = listOf(
    ReleaseOutside28WeeksRule::class.java.simpleName,
  )

  @Test
  fun `all rule components are included in rule set`() {
    val ruleSetRules = ruleSet.getRules().map { it.rule.javaClass.simpleName }

    assertThat(ruleSetRules)
      .hasSize(1)
      .containsExactlyInAnyOrderElementsOf(expectedRuleNames)
  }
}
