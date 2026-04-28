package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.dtr.eligibility

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.NextAccommodationRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.eligibility.CurrentAddressTypeNotPrivateRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.eligibility.DtrEligibilityRuleSet

@ExtendWith(SpringExtension::class)
@ContextConfiguration(
  classes = [
    DtrEligibilityRuleSet::class,
    NextAccommodationRule::class,
    CurrentAddressTypeNotPrivateRule::class,
  ],
)
class DtrEligibilityRuleSetTest {

  @Autowired
  lateinit var dtrEligibilityRuleSet: DtrEligibilityRuleSet

  private val expectedDtrEligibilityRuleNames = listOf(
    NextAccommodationRule::class.simpleName,
    CurrentAddressTypeNotPrivateRule::class.simpleName,
  )

  @Test
  fun `all DtrEligibilityRule components are included in DtrEligibilityRuleSet`() {
    val ruleSetRules = dtrEligibilityRuleSet.getRules().map { it.javaClass.simpleName }

    assertThat(ruleSetRules)
      .hasSize(2)
      .containsExactlyInAnyOrderElementsOf(expectedDtrEligibilityRuleNames)
  }
}
