package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.crs.eligibility

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.config.ClockConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.CurrentAccommodationEndDateValidationRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.NextAccommodationRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.eligibility.CrsEligibilityRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.eligibility.IsMaleRule

@ExtendWith(SpringExtension::class)
@ContextConfiguration(
  classes = [
    CrsEligibilityRuleSet::class,
    NextAccommodationRule::class,
    CurrentAccommodationEndDateValidationRule::class,
    IsMaleRule::class,
    ClockConfig::class,
  ],
)
class CrsEligibilityRuleSetTest {

  @Autowired
  lateinit var crsEligibilityRuleSet: CrsEligibilityRuleSet

  private val expectedCrsEligibilityRuleNames = listOf(
    IsMaleRule::class.simpleName,
    NextAccommodationRule::class.simpleName,
    CurrentAccommodationEndDateValidationRule::class.simpleName,
  )

  @Test
  fun `all CrsEligibilityRule components are included in CrsEligibilityRuleSet`() {
    val ruleSetRules = crsEligibilityRuleSet.getRules().map { it.javaClass.simpleName }

    assertThat(ruleSetRules)
      .hasSize(3)
      .containsExactlyInAnyOrderElementsOf(expectedCrsEligibilityRuleNames)
  }
}
