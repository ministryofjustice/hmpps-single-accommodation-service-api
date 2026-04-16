package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.dtr.suitability

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.config.ClockConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.DtrExpiredReferralRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.DtrPresentRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.DtrStatusRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.suitability.DtrSuitabilityRuleSet

@ExtendWith(SpringExtension::class)
@ContextConfiguration(
  classes = [
    DtrSuitabilityRuleSet::class,
    DtrStatusRule::class,
    DtrPresentRule::class,
    DtrExpiredReferralRule::class,
    ClockConfig::class,
  ],
)
class DtrSuitabilityRuleSetTest {

  @Autowired
  lateinit var dtrSuitabilityRuleSet: DtrSuitabilityRuleSet

  private val expectedDtrSuitabilityRuleNames = listOf(
    DtrStatusRule::class.simpleName,
    DtrPresentRule::class.simpleName,
    DtrExpiredReferralRule::class.simpleName,
  )

  @Test
  fun `all DtrSuitabilityRule components are included in DtrSuitabilityRuleSet`() {
    val ruleSetRules = dtrSuitabilityRuleSet.getRules().map { it.javaClass.simpleName }

    assertThat(ruleSetRules)
      .hasSize(3)
      .containsExactlyInAnyOrderElementsOf(expectedDtrSuitabilityRuleNames)
  }
}
