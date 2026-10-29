package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas3.referral

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.config.ClockConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.referral.Cas3RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.referral.rules.Cas3AssessmentNotActiveRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.referral.rules.Cas3BookingNotActiveRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.referral.rules.Cas3NotPresentRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.referral.rules.Cas3ReferralNotActiveRule

@ExtendWith(SpringExtension::class)
@ContextConfiguration(
  classes = [
    Cas3RuleSet::class,
    Cas3NotPresentRule::class,
    Cas3BookingNotActiveRule::class,
    Cas3ReferralNotActiveRule::class,
    Cas3AssessmentNotActiveRule::class,
    ClockConfig::class,
  ],
)
class Cas3RuleSetTest {

  @Autowired
  lateinit var ruleSet: Cas3RuleSet

  private val expectedRuleNames = listOf(
    Cas3NotPresentRule::class.java.simpleName,
    Cas3BookingNotActiveRule::class.java.simpleName,
    Cas3ReferralNotActiveRule::class.java.simpleName,
    Cas3AssessmentNotActiveRule::class.java.simpleName,
  )

  @Test
  fun `all rule components are included in rule set`() {
    val ruleSetRules = ruleSet.getRules().map { it.rule.javaClass.simpleName }

    assertThat(ruleSetRules)
      .hasSize(4)
      .containsExactlyInAnyOrderElementsOf(expectedRuleNames)
  }
}
