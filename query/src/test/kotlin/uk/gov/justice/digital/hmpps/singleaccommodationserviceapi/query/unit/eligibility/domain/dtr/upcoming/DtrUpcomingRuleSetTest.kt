package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.dtr.upcoming

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.config.ClockConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.upcoming.DtrRecentCurrentAccommodationEndDateRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.upcoming.DtrRecentReleaseDateRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.upcoming.DtrUpcomingRuleSet

@ExtendWith(SpringExtension::class)
@ContextConfiguration(
  classes = [
    DtrRecentReleaseDateRule::class,
    DtrRecentCurrentAccommodationEndDateRule::class,
    DtrUpcomingRuleSet::class,
    ClockConfig::class,
  ],
)
class DtrUpcomingRuleSetTest {

  @Autowired
  lateinit var dtrUpcomingRuleSet: DtrUpcomingRuleSet

  private val expectedDtrUpcomingRuleNames = listOf(
    DtrRecentReleaseDateRule::class.simpleName,
    DtrRecentCurrentAccommodationEndDateRule::class.simpleName,
  )

  @Test
  fun `all DtrUpcomingRule components are included in DtrUpcomingRuleSet`() {
    val ruleSetRules = dtrUpcomingRuleSet.getRules().map { it.javaClass.simpleName }

    assertThat(ruleSetRules)
      .hasSize(2)
      .containsExactlyInAnyOrderElementsOf(expectedDtrUpcomingRuleNames)
  }
}
