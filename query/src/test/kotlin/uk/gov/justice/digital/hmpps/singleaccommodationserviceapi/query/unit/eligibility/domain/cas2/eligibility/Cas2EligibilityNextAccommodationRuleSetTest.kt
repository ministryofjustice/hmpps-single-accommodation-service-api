package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas2.eligibility

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.config.ClockConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligibility.Cas2EligibilityNextAccommodationRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.NoNextAccommodationRule

@ExtendWith(SpringExtension::class)
@ContextConfiguration(
  classes = [
    Cas2EligibilityNextAccommodationRuleSet::class,
    NoNextAccommodationRule::class,
    ClockConfig::class,
  ],
)
class Cas2EligibilityNextAccommodationRuleSetTest {

  @Autowired
  lateinit var cas2EligibilityNextAccommodationRuleSet: Cas2EligibilityNextAccommodationRuleSet

  private val expectedRuleNames = listOf(
    NoNextAccommodationRule::class.java.simpleName,
  )

  @Test
  fun `all expected rules are included in Cas2EligibilityNextAccommodationRuleSet`() {
    val ruleSetRules = cas2EligibilityNextAccommodationRuleSet.getRules().map { it.rule.javaClass.simpleName }

    assertThat(ruleSetRules)
      .hasSize(1)
      .containsExactlyInAnyOrderElementsOf(expectedRuleNames)
  }
}
