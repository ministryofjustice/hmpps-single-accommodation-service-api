package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas1.eligible

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.config.ClockConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.eligible.TierEligibleForCas1RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.eligible.rules.MaleRiskEligibilityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.eligible.rules.NonMaleRiskEligibilityRule

@ExtendWith(SpringExtension::class)
@ContextConfiguration(
  classes = [
    TierEligibleForCas1RuleSet::class,
    MaleRiskEligibilityRule::class,
    NonMaleRiskEligibilityRule::class,
    ClockConfig::class,
  ],
)
class TierEligibleForCas1RuleSetTest {

  @Autowired
  lateinit var ruleSet: TierEligibleForCas1RuleSet

  private val expectedRuleNames = listOf(
    MaleRiskEligibilityRule::class.java.simpleName,
    NonMaleRiskEligibilityRule::class.java.simpleName,
  )

  @Test
  fun `all rule components are included in rule set`() {
    val ruleSetRules = ruleSet.getRules().map { it.rule.javaClass.simpleName }

    assertThat(ruleSetRules)
      .hasSize(2)
      .containsExactlyInAnyOrderElementsOf(expectedRuleNames)
  }
}
