package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas1.eligibility

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.config.ClockConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.eligibility.Cas1EligibilityRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.eligibility.CurrentAccommodationIsPrisonOrCas1Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.eligibility.MaleRiskEligibilityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.eligibility.NonMaleRiskEligibilityRule

@ExtendWith(SpringExtension::class)
@ContextConfiguration(
  classes = [
    Cas1EligibilityRuleSet::class,
    MaleRiskEligibilityRule::class,
    NonMaleRiskEligibilityRule::class,
    CurrentAccommodationIsPrisonOrCas1Rule::class,
    ClockConfig::class,
  ],
)
class Cas1EligibilityRuleSetTest {

  @Autowired
  lateinit var cas1EligibilityRuleSet: Cas1EligibilityRuleSet

  private val expectedCas1EligibilityRuleNames = listOf(
    MaleRiskEligibilityRule::class.java.simpleName,
    NonMaleRiskEligibilityRule::class.java.simpleName,
    CurrentAccommodationIsPrisonOrCas1Rule::class.java.simpleName,
  )

  @Test
  fun `all Cas1EligibilityRule components are included in Cas1EligibilityRuleSet`() {
    val ruleSetRules = cas1EligibilityRuleSet.getRules().map { it.rule.javaClass.simpleName }

    assertThat(ruleSetRules)
      .hasSize(3)
      .containsExactlyInAnyOrderElementsOf(expectedCas1EligibilityRuleNames)
  }

  @Test
  fun `current accommodation rule has priority 1`() {
    val rule = cas1EligibilityRuleSet.getRules()
      .single { it.rule is CurrentAccommodationIsPrisonOrCas1Rule }

    assertThat(rule.priority).isEqualTo(1)
  }
}
