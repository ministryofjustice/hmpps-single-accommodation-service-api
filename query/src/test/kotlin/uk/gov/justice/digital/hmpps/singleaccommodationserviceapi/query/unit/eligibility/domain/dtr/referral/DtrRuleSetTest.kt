package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.dtr.referral

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.config.ClockConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.rules.DtrExpiredRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.referral.DtrRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.referral.rules.DtrNotPresentRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.referral.rules.DtrWithdrawnRule

@ExtendWith(SpringExtension::class)
@ContextConfiguration(
  classes = [
    DtrRuleSet::class,
    DtrNotPresentRule::class,
    DtrWithdrawnRule::class,
    DtrExpiredRule::class,
    ClockConfig::class,
  ],
)
class DtrRuleSetTest {

  @Autowired
  lateinit var ruleSet: DtrRuleSet

  private val expectedRuleNames = listOf(
    DtrNotPresentRule::class.java.simpleName,
    DtrWithdrawnRule::class.java.simpleName,
    DtrExpiredRule::class.java.simpleName,
  )

  @Test
  fun `all rule components are included in rule set`() {
    val ruleSetRules = ruleSet.getRules().map { it.rule.javaClass.simpleName }

    assertThat(ruleSetRules)
      .hasSize(3)
      .containsExactlyInAnyOrderElementsOf(expectedRuleNames)
  }
}
