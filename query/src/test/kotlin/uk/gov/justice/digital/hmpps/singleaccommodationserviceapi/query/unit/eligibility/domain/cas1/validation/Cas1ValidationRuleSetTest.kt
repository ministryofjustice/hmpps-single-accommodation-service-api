package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas1.validation

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.config.ClockConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.validation.Cas1SexValidationRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.validation.Cas1ValidationRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.CurrentAccommodationEndDateValidationRule

@ExtendWith(SpringExtension::class)
@ContextConfiguration(
  classes = [
    Cas1ValidationRuleSet::class,
    CurrentAccommodationEndDateValidationRule::class,
    Cas1SexValidationRule::class,
    ClockConfig::class,
  ],
)
class Cas1ValidationRuleSetTest {

  @Autowired
  lateinit var cas1ValidationRuleSet: Cas1ValidationRuleSet

  private val expectedCas1ValidationRuleNames = listOf(
    CurrentAccommodationEndDateValidationRule::class.simpleName,
    Cas1SexValidationRule::class.simpleName,
  )

  @Test
  fun `all Cas1ValidationRule components are included in Cas1ValidationRuleSet`() {
    val ruleSetRules = cas1ValidationRuleSet.getRules().map { it.javaClass.simpleName }

    assertThat(ruleSetRules)
      .hasSize(2)
      .containsExactlyInAnyOrderElementsOf(expectedCas1ValidationRuleNames)
  }
}
