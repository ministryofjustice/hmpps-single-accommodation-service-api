package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas3.eligibility

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.config.ClockConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.eligibility.Cas3EligibilityCurrentAccommodationRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.eligibility.CurrentAccommodationTypeRule

@ExtendWith(SpringExtension::class)
@ContextConfiguration(
  classes = [
    Cas3EligibilityCurrentAccommodationRuleSet::class,
    CurrentAccommodationTypeRule::class,
    ClockConfig::class,
  ],
)
class Cas3EligibilityCurrentAccommodationRuleSetTest {

  @Autowired
  lateinit var cas3EligibilityCurrentAccommodationRuleSet: Cas3EligibilityCurrentAccommodationRuleSet

  private val expectedRuleNames = listOf(
    CurrentAccommodationTypeRule::class.java.simpleName,
  )

  @Test
  fun `all expected rules are included in Cas3EligibilityCurrentAccommodationRuleSet`() {
    val ruleSetRules = cas3EligibilityCurrentAccommodationRuleSet.getRules().map { it.rule.javaClass.simpleName }

    assertThat(ruleSetRules)
      .hasSize(1)
      .containsExactlyInAnyOrderElementsOf(expectedRuleNames)
  }
}
