package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas2.eligibility

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.config.ClockConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligibility.Cas2EligibilityRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligibility.CurrentAccommodationTypeIsCas2Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligibility.CurrentAccommodationTypeIsCas3Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligibility.CurrentAccommodationTypeIsCommunityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligibility.Under18Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.NoNextAccommodationRule

@ExtendWith(SpringExtension::class)
@ContextConfiguration(
  classes = [
    Cas2EligibilityRuleSet::class,
    CurrentAccommodationTypeIsCas3Rule::class,
    CurrentAccommodationTypeIsCas2Rule::class,
    CurrentAccommodationTypeIsCommunityRule::class,
    ClockConfig::class,
    Under18Rule::class,
    NoNextAccommodationRule::class,
  ],
)
class Cas2EligibilityRuleSetTest {

  @Autowired
  lateinit var cas2EligibilityRuleSet: Cas2EligibilityRuleSet

  private val expectedCas2EligibilityRuleNames = listOf(
    Under18Rule::class.java.simpleName,
    NoNextAccommodationRule::class.java.simpleName,
    CurrentAccommodationTypeIsCas3Rule::class.java.simpleName,
    CurrentAccommodationTypeIsCas2Rule::class.java.simpleName,
    CurrentAccommodationTypeIsCommunityRule::class.java.simpleName,
  )

  @Test
  fun `all Cas2EligibilityRule components are included in Cas2EligibilityRuleSet`() {
    val ruleSetRules = cas2EligibilityRuleSet.getRules().map { it.rule.javaClass.simpleName }

    assertThat(ruleSetRules)
      .hasSize(5)
      .containsExactlyInAnyOrderElementsOf(expectedCas2EligibilityRuleNames)
  }
}
