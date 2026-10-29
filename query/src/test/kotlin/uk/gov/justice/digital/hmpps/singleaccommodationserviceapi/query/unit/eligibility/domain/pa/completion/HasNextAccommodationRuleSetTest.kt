package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.pa.completion

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.config.ClockConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.pa.referral.HasNextAccommodationRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.pa.referral.rules.NoNextAccommodationRule

@ExtendWith(SpringExtension::class)
@ContextConfiguration(
  classes = [
    HasNextAccommodationRuleSet::class,
    NoNextAccommodationRule::class,
    ClockConfig::class,
  ],
)
class HasNextAccommodationRuleSetTest {

  @Autowired
  lateinit var ruleSet: HasNextAccommodationRuleSet

  private val expectedRuleNames = listOf(
    NoNextAccommodationRule::class.java.simpleName,
  )

  @Test
  fun `all rule components are included in rule set`() {
    val ruleSetRules = ruleSet.getRules().map { it.rule.javaClass.simpleName }

    assertThat(ruleSetRules)
      .hasSize(1)
      .containsExactlyInAnyOrderElementsOf(expectedRuleNames)
  }
}
