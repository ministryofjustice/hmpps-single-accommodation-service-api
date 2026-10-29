package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas2.eligibility

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.config.ClockConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligibility.Cas2EligibilityCurrentAccommodationRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligibility.CurrentAccommodationTypeIsCas2Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligibility.CurrentAccommodationTypeIsCas3Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligibility.CurrentAccommodationTypeIsCommunityRule

@ExtendWith(SpringExtension::class)
@ContextConfiguration(
  classes = [
    Cas2EligibilityCurrentAccommodationRuleSet::class,
    CurrentAccommodationTypeIsCas3Rule::class,
    CurrentAccommodationTypeIsCas2Rule::class,
    CurrentAccommodationTypeIsCommunityRule::class,
    ClockConfig::class,
  ],
)
class Cas2EligibilityCurrentAccommodationRuleSetTest {

  @Autowired
  lateinit var cas2EligibilityCurrentAccommodationRuleSet: Cas2EligibilityCurrentAccommodationRuleSet

  private val expectedRuleNames = listOf(
    CurrentAccommodationTypeIsCas3Rule::class.java.simpleName,
    CurrentAccommodationTypeIsCas2Rule::class.java.simpleName,
    CurrentAccommodationTypeIsCommunityRule::class.java.simpleName,
  )

  @Test
  fun `all expected rules are included in Cas2EligibilityCurrentAccommodationRuleSet`() {
    val ruleSetRules = cas2EligibilityCurrentAccommodationRuleSet.getRules().map { it.rule.javaClass.simpleName }

    assertThat(ruleSetRules)
      .hasSize(3)
      .containsExactlyInAnyOrderElementsOf(expectedRuleNames)
  }
}
