package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas2.referral

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.config.ClockConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.referral.Cas2RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.referral.rules.Cas2NotActiveRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.referral.rules.Cas2NotSubmittedRule

@ExtendWith(SpringExtension::class)
@ContextConfiguration(
  classes = [
    Cas2RuleSet::class,
    Cas2NotSubmittedRule::class,
    Cas2NotActiveRule::class,
    ClockConfig::class,
  ],
)
class Cas2RuleSetTest {

  @Autowired
  lateinit var ruleSet: Cas2RuleSet

  private val expectedRuleNames = listOf(
    Cas2NotSubmittedRule::class.java.simpleName,
    Cas2NotActiveRule::class.java.simpleName,
  )

  @Test
  fun `all rule components are included in rule set`() {
    val ruleSetRules = ruleSet.getRules().map { it.rule.javaClass.simpleName }

    assertThat(ruleSetRules)
      .hasSize(2)
      .containsExactlyInAnyOrderElementsOf(expectedRuleNames)
  }
}
