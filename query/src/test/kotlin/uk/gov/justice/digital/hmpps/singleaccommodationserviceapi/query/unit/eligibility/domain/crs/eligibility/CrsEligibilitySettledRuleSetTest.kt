package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.crs.eligibility

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.config.ClockConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.eligibility.CrsEligibilitySettledRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.eligibility.IsSettledRule

@ExtendWith(SpringExtension::class)
@ContextConfiguration(
  classes = [
    CrsEligibilitySettledRuleSet::class,
    IsSettledRule::class,
    ClockConfig::class,
  ],
)
class CrsEligibilitySettledRuleSetTest {

  @Autowired
  lateinit var crsEligibilitySettledRuleSet: CrsEligibilitySettledRuleSet

  private val expectedRuleNames = listOf(
    IsSettledRule::class.java.simpleName,
  )

  @Test
  fun `all expected rules are included in CrsEligibilitySettledRuleSet`() {
    val ruleSetRules = crsEligibilitySettledRuleSet.getRules().map { it.rule.javaClass.simpleName }

    assertThat(ruleSetRules)
      .hasSize(1)
      .containsExactlyInAnyOrderElementsOf(expectedRuleNames)
  }
}
