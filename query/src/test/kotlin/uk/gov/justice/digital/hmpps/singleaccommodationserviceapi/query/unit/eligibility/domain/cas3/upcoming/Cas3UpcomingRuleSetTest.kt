package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas3.upcoming

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.config.ClockConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.upcoming.Cas3RecentCurrentAccommodationEndDateRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.upcoming.Cas3UpcomingRuleSet

@ExtendWith(SpringExtension::class)
@ContextConfiguration(
  classes = [
    Cas3RecentCurrentAccommodationEndDateRule::class,
    Cas3UpcomingRuleSet::class,
    ClockConfig::class,
  ],
)
class Cas3UpcomingRuleSetTest {

  @Autowired
  lateinit var cas3UpcomingRuleSet: Cas3UpcomingRuleSet

  private val expectedCas3UpcomingRuleNames = listOf(
    Cas3RecentCurrentAccommodationEndDateRule::class.simpleName,
  )

  @Test
  fun `all Cas3UpcomingRule components are included in Cas3UpcomingRuleSet`() {
    val ruleSetRules = cas3UpcomingRuleSet.getRules().map { it.javaClass.simpleName }

    assertThat(ruleSetRules)
      .hasSize(1)
      .containsExactlyInAnyOrderElementsOf(expectedCas3UpcomingRuleNames)
  }
}
