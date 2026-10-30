package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.dtr.upcoming

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.config.ClockConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.upcoming.ReleaseWithin8WeeksRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.upcoming.rules.ReleaseOutside8WeeksRule

@ExtendWith(SpringExtension::class)
@ContextConfiguration(
  classes = [
    ReleaseOutside8WeeksRule::class,
    ReleaseWithin8WeeksRuleSet::class,
    ClockConfig::class,
  ],
)
class ReleaseWithin8WeeksRuleSetTest {

  @Autowired
  lateinit var ruleSet: ReleaseWithin8WeeksRuleSet

  private val expectedRuleNames = listOf(
    ReleaseOutside8WeeksRule::class.java.simpleName,
  )

  @Test
  fun `all rule components are included in rule set`() {
    val ruleSetRules = ruleSet.getRules().map { it.rule.javaClass.simpleName }

    assertThat(ruleSetRules)
      .hasSize(1)
      .containsExactlyInAnyOrderElementsOf(expectedRuleNames)
  }
}
