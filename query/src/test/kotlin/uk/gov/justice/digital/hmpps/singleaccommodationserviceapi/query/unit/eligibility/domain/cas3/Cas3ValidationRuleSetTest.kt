package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas3

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.config.ClockConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.Cas3ValidationRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.rules.Cas3ReleaseDateValidationRule

@ExtendWith(SpringExtension::class)
@ContextConfiguration(
  classes = [
    Cas3ValidationRuleSet::class,
    Cas3ReleaseDateValidationRule::class,
    ClockConfig::class,
  ],
)
class Cas3ValidationRuleSetTest {

  @Autowired
  lateinit var cas3ValidationRuleSet: Cas3ValidationRuleSet

  private val expectedCas3ValidationRuleNames = listOf(
    Cas3ReleaseDateValidationRule::class.simpleName,
  )

  @Test
  fun `all Cas3ValidationRule components are included in Cas3ValidationRuleSet`() {
    val ruleSetRules = cas3ValidationRuleSet.getRules().map { it.javaClass.simpleName }

    assertThat(ruleSetRules)
      .hasSize(1)
      .containsExactlyInAnyOrderElementsOf(expectedCas3ValidationRuleNames)
  }
}
