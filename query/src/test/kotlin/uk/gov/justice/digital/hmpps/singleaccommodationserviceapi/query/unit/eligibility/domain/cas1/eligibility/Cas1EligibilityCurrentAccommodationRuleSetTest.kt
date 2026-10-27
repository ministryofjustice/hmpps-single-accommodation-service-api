package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas1.eligibility

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.config.ClockConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.eligibility.Cas1EligibilityCurrentAccommodationRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.eligibility.CurrentAccommodationIsPrisonOrCas1Rule

@ExtendWith(SpringExtension::class)
@ContextConfiguration(
  classes = [
    Cas1EligibilityCurrentAccommodationRuleSet::class,
    CurrentAccommodationIsPrisonOrCas1Rule::class,
    ClockConfig::class,
  ],
)
class Cas1EligibilityCurrentAccommodationRuleSetTest {

  @Autowired
  lateinit var cas1EligibilityCurrentAccommodationRuleSet: Cas1EligibilityCurrentAccommodationRuleSet

  private val expectedCas1EligibilityRuleNames = listOf(
    CurrentAccommodationIsPrisonOrCas1Rule::class.java.simpleName,
  )

  @Test
  fun `all Cas1EligibilityRule components are included in Cas1EligibilityHighPriorityRuleSet`() {
    val ruleSetRules = cas1EligibilityCurrentAccommodationRuleSet.getRules().map { it.rule.javaClass.simpleName }

    assertThat(ruleSetRules)
      .hasSize(1)
      .containsExactlyInAnyOrderElementsOf(expectedCas1EligibilityRuleNames)
  }
}
