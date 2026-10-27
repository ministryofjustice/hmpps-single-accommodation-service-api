package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas3.upcoming

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.config.ClockConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.upcoming.Cas3UpcomingRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.ReleaseWithin15DaysRule

@ExtendWith(SpringExtension::class)
@ContextConfiguration(
  classes = [
    ReleaseWithin15DaysRule::class,
    Cas3UpcomingRuleSet::class,
    ClockConfig::class,
  ],
)
class Cas3UpcomingRuleSetTest {
  @Autowired
  lateinit var cas3UpcomingRuleSet: Cas3UpcomingRuleSet

  @Test
  fun `CAS3 upcoming rule set contains the release within 15 days rule`() {
    assertThat(cas3UpcomingRuleSet.getRules().map { it.rule.javaClass.simpleName })
      .containsExactly(ReleaseWithin15DaysRule::class.java.simpleName)
  }
}
