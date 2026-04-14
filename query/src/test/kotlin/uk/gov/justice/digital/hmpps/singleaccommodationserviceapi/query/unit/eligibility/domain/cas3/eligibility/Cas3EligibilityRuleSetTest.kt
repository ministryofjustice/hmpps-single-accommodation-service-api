package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas3.eligibility

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.config.ClockConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.eligibility.Cas3EligibilityRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.eligibility.CrsStatusRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.eligibility.CurrentAccommodationTypeRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.eligibility.NoConflictingCas1BookingRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.DtrExpiredReferralRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.NextAccommodationRule

@ExtendWith(SpringExtension::class)
@ContextConfiguration(
  classes = [
    Cas3EligibilityRuleSet::class,
    CurrentAccommodationTypeRule::class,
    NextAccommodationRule::class,
    CrsStatusRule::class,
    DtrExpiredReferralRule::class,
    NoConflictingCas1BookingRule::class,
    ClockConfig::class,
  ],
)
class Cas3EligibilityRuleSetTest {

  @Autowired
  lateinit var cas3EligibilityRuleSet: Cas3EligibilityRuleSet

  private val expectedCas3EligibilityRuleNames = listOf(
    CurrentAccommodationTypeRule::class.simpleName,
    NextAccommodationRule::class.simpleName,
    CrsStatusRule::class.simpleName,
    DtrExpiredReferralRule::class.simpleName,
    NoConflictingCas1BookingRule::class.simpleName,
  )

  @Test
  fun `all Cas3EligibilityRule components are included in Cas3EligibilityRuleSet`() {
    val ruleSetRules = cas3EligibilityRuleSet.getRules().map { it.javaClass.simpleName }

    assertThat(ruleSetRules)
      .hasSize(5)
      .containsExactlyInAnyOrderElementsOf(expectedCas3EligibilityRuleNames)
  }
}
