package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.crs.eligibility

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.config.ClockConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.NoNextAccommodationRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.eligibility.CrsEligibilityNextAccommodationRuleSet

@ExtendWith(SpringExtension::class)
@ContextConfiguration(
  classes = [
    CrsEligibilityNextAccommodationRuleSet::class,
    NoNextAccommodationRule::class,
    ClockConfig::class,
  ],
)
class CrsEligibilityNextAccommodationRuleSetTest {

  @Autowired
  lateinit var crsEligibilityNextAccommodationRuleSet: CrsEligibilityNextAccommodationRuleSet

  private val expectedRuleNames = listOf(
    NoNextAccommodationRule::class.java.simpleName,
  )

  @Test
  fun `all expected rules are included in CrsEligibilityNextAccommodationRuleSet`() {
    val ruleSetRules = crsEligibilityNextAccommodationRuleSet.getRules().map { it.rule.javaClass.simpleName }

    assertThat(ruleSetRules)
      .hasSize(1)
      .containsExactlyInAnyOrderElementsOf(expectedRuleNames)
  }
}
