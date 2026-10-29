package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas1.referral

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.config.ClockConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.referral.Cas1RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.referral.rules.Cas1ExpiredRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.referral.rules.Cas1NotActiveRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.referral.rules.Cas1NotPresentRule

@ExtendWith(SpringExtension::class)
@ContextConfiguration(
  classes = [
    Cas1RuleSet::class,
    Cas1NotActiveRule::class,
    Cas1ExpiredRule::class,
    Cas1NotPresentRule::class,
    ClockConfig::class,
  ],
)
class Cas1RuleSetTest {

  @Autowired
  lateinit var ruleSet: Cas1RuleSet

  private val expectedRuleNames = listOf(
    Cas1NotActiveRule::class.java.simpleName,
    Cas1NotPresentRule::class.java.simpleName,
    Cas1ExpiredRule::class.java.simpleName,
  )

  @Test
  fun `all rule components are included in rule set`() {
    val ruleSetRules = ruleSet.getRules().map { it.rule.javaClass.simpleName }

    assertThat(ruleSetRules)
      .hasSize(3)
      .containsExactlyInAnyOrderElementsOf(expectedRuleNames)
  }
}
