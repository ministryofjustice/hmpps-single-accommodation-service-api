package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.eligibility

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.CrsExpiredRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.CrsSubmittedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.DtrExpiredReferralRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.NoNextAccommodationRule

@Component
class Cas3EligibilityRuleSet(
  currentAccommodationType: CurrentAccommodationTypeRule,
  noNextAccommodation: NoNextAccommodationRule,
  dtrExpiredReferral: DtrExpiredReferralRule,
  noConflictingCas1Booking: NoConflictingCas1BookingRule,
  crsSubmitted: CrsSubmittedRule,
  crsExpired: CrsExpiredRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(
    currentAccommodationType,
    noNextAccommodation,
    dtrExpiredReferral,
    noConflictingCas1Booking,
    crsExpired,
    crsSubmitted,
  )
  override fun getRules(): List<Rule> = rules
}
