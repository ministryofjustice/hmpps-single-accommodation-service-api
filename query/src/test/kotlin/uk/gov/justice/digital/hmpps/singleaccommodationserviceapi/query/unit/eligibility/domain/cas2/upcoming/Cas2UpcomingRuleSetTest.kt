package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas2.upcoming

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.config.ClockConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.upcoming.ReleaseWithinOneYearRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.upcoming.Cas2UpcomingRuleSet

@ExtendWith(SpringExtension::class)
@ContextConfiguration(
  classes = [
    ReleaseWithinOneYearRule::class,
    Cas2UpcomingRuleSet::class,
    ClockConfig::class,
  ],
)
class Cas2UpcomingRuleSetTest {

  @Autowired
  lateinit var cas2UpcomingRuleSet: Cas2UpcomingRuleSet

  private val expectedCas2UpcomingRuleNames = listOf(
    ReleaseWithinOneYearRule::class.simpleName,
  )

  @Test
  fun `all Cas2UpcomingRule components are included in Cas2UpcomingRuleSet`() {
    val ruleSetRules = cas2UpcomingRuleSet.getRules().map { it.javaClass.simpleName }

    assertThat(ruleSetRules)
      .hasSize(1)
      .containsExactlyInAnyOrderElementsOf(expectedCas2UpcomingRuleNames)
  }
}
