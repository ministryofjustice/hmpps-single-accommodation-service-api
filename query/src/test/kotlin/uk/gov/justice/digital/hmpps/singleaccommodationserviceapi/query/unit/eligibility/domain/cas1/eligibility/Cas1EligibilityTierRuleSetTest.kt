package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas1.eligibility

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.config.ClockConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.eligibility.Cas1EligibilityTierRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.eligibility.MaleRiskEligibilityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.eligibility.NonMaleRiskEligibilityRule

@ExtendWith(SpringExtension::class)
@ContextConfiguration(
  classes = [
    Cas1EligibilityTierRuleSet::class,
    MaleRiskEligibilityRule::class,
    NonMaleRiskEligibilityRule::class,
    ClockConfig::class,
  ],
)
class Cas1EligibilityTierRuleSetTest {

  @Autowired
  lateinit var cas1EligibilityTierRuleSet: Cas1EligibilityTierRuleSet

  private val expectedCas1EligibilityRuleNames = listOf(
    MaleRiskEligibilityRule::class.java.simpleName,
    NonMaleRiskEligibilityRule::class.java.simpleName,
  )

  @Test
  fun `all Cas1EligibilityRule components are included in Cas1EligibilityHighPriorityRuleSet`() {
    val ruleSetRules = cas1EligibilityTierRuleSet.getRules().map { it.rule.javaClass.simpleName }

    assertThat(ruleSetRules)
      .hasSize(2)
      .containsExactlyInAnyOrderElementsOf(expectedCas1EligibilityRuleNames)
  }
}
