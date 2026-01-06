package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas3

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.config.ClockConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.Cas3EligibilityRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.rules.CrsStatusRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.rules.CurrentAddressTypeRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.rules.DtrStatusRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.rules.ExistingCas1BookingRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.rules.NextAccommodationRule

@ExtendWith(SpringExtension::class)
@ContextConfiguration(
  classes = [
    Cas3EligibilityRuleSet::class,
    CurrentAddressTypeRule::class,
    NextAccommodationRule::class,
    CrsStatusRule::class,
    DtrStatusRule::class,
    ExistingCas1BookingRule::class,
    ClockConfig::class,
  ],
)
class Cas3EligibilityRuleSetTest {

  @Autowired
  lateinit var cas3EligibilityRuleSet: Cas3EligibilityRuleSet

  private val expectedCas3EligibilityRuleNames = listOf(
    CurrentAddressTypeRule::class.simpleName,
    NextAccommodationRule::class.simpleName,
    CrsStatusRule::class.simpleName,
    DtrStatusRule::class.simpleName,
    ExistingCas1BookingRule::class.simpleName,
  )

  @Test
  fun `all Cas3EligibilityRule components are included in Cas3EligibilityRuleSet`() {
    val ruleSetRules = cas3EligibilityRuleSet.getRules().map { it.javaClass.simpleName }

    assertThat(ruleSetRules)
      .hasSize(5)
      .containsExactlyInAnyOrderElementsOf(expectedCas3EligibilityRuleNames)
  }
}
