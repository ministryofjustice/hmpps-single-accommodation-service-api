package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.referral

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.referral.rules.Cas3AssessmentNotActiveRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.referral.rules.Cas3BookingNotActiveRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.referral.rules.Cas3NotPresentRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.referral.rules.Cas3ReferralNotActiveRule

@Component
class Cas3RuleSet(
  cas3ReferralNotActive: Cas3ReferralNotActiveRule,
  cas3NotPresent: Cas3NotPresentRule,
  cas3BookingNotActive: Cas3BookingNotActiveRule,
  cas3AssessmentNotActive: Cas3AssessmentNotActiveRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(
    cas3ReferralNotActive,
    cas3NotPresent,
    cas3BookingNotActive,
    cas3AssessmentNotActive,
  )

  override val name: String = "CAS3 Referral"

  override fun getRules(): List<Rule> = rules
}
