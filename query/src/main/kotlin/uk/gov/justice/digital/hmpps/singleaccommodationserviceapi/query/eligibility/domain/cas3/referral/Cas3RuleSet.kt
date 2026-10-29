package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.referral

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.PrioritisedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.referral.rules.Cas3AssessmentNotActiveRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.referral.rules.Cas3BookingNotActiveRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.referral.rules.Cas3NotPresentRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.referral.rules.Cas3ReferralNotActiveRule

@Component
class Cas3RuleSet(
  applicationSuitability: Cas3ReferralNotActiveRule,
  applicationPresentSuitability: Cas3NotPresentRule,
  bookingSuitability: Cas3BookingNotActiveRule,
  assessmentSuitability: Cas3AssessmentNotActiveRule,
) : RuleSet {
  private val rules: List<PrioritisedRule> = listOf(
    PrioritisedRule(applicationSuitability),
    PrioritisedRule(applicationPresentSuitability),
    PrioritisedRule(bookingSuitability),
    PrioritisedRule(assessmentSuitability),
  )

  override val name: String = "Suitability"

  override fun getRules(): List<PrioritisedRule> = rules
}
