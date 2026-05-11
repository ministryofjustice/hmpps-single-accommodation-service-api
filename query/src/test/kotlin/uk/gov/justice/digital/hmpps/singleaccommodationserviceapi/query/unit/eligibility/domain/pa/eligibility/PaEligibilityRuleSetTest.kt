package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.pa.eligibility

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.pa.eligibility.Cas3ApplicationNotSuitableRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.pa.eligibility.PaEligibilityRuleSet

@ExtendWith(SpringExtension::class)
@ContextConfiguration(
  classes = [
    PaEligibilityRuleSet::class,
  ],
)
class PaEligibilityRuleSetTest {

  @Autowired
  lateinit var paEligibilityRuleSet: PaEligibilityRuleSet

  private val expectedPaEligibilityRuleNames = listOf<String>()

  @Test
  fun `all PaEligibilityRule components are included in PaEligibilityRuleSet`() {
    val ruleSetRules = paEligibilityRuleSet.getRules().map { it.javaClass.simpleName }

    assertThat(ruleSetRules)
      .hasSize(0)
      .containsExactlyInAnyOrderElementsOf(expectedPaEligibilityRuleNames)
  }
}
