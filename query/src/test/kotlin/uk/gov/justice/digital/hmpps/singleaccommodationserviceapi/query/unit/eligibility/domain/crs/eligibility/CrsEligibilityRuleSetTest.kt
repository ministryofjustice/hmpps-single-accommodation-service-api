package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.crs.eligibility

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.config.ClockConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.accommodation.NoNextAccommodationRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.eligibility.CrsEligibilityRuleSet

@ExtendWith(SpringExtension::class)
@ContextConfiguration(
  classes = [
    CrsEligibilityRuleSet::class,
    NoNextAccommodationRule::class,
    ClockConfig::class,
  ],
)
class CrsEligibilityRuleSetTest {

  @Autowired
  lateinit var crsEligibilityRuleSet: CrsEligibilityRuleSet

  private val expectedCrsEligibilityRuleNames = listOf(
    NoNextAccommodationRule::class.simpleName,
  )

  @Test
  fun `all CrsEligibilityRule components are included in CrsEligibilityRuleSet`() {
    val ruleSetRules = crsEligibilityRuleSet.getRules().map { it.javaClass.simpleName }

    assertThat(ruleSetRules)
      .hasSize(1)
      .containsExactlyInAnyOrderElementsOf(expectedCrsEligibilityRuleNames)
  }
}
