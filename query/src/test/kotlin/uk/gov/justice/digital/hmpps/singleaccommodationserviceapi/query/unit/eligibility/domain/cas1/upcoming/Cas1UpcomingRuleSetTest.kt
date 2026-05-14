package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas1.upcoming

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.config.ClockConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.upcoming.Cas1UpcomingRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.upcoming.ReleaseWithinOneYearRule

@ExtendWith(SpringExtension::class)
@ContextConfiguration(
  classes = [
    ReleaseWithinOneYearRule::class,
    Cas1UpcomingRuleSet::class,
    ClockConfig::class,
  ],
)
class Cas1UpcomingRuleSetTest {

  @Autowired
  lateinit var cas1UpcomingRuleSet: Cas1UpcomingRuleSet

  private val expectedCas1UpcomingRuleNames = listOf(
    ReleaseWithinOneYearRule::class.simpleName,
  )

  @Test
  fun `all Cas1UpcomingRule components are included in Cas1UpcomingRuleSet`() {
    val ruleSetRules = cas1UpcomingRuleSet.getRules().map { it.javaClass.simpleName }

    assertThat(ruleSetRules)
      .hasSize(1)
      .containsExactlyInAnyOrderElementsOf(expectedCas1UpcomingRuleNames)
  }
}
