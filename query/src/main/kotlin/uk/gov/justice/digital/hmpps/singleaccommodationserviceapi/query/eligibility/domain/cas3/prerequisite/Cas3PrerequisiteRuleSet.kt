package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.prerequisite

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.CrsExpiredRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.CrsSubmittedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.DtrExpiredReferralRule

@Component
class Cas3PrerequisiteRuleSet(
  dtrExpiredReferral: DtrExpiredReferralRule,
  crsExpired: CrsExpiredRule,
  crsSubmitted: CrsSubmittedRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(
    dtrExpiredReferral,
    crsExpired,
    crsSubmitted,
  )

  override fun getRules(): List<Rule> = rules
}
