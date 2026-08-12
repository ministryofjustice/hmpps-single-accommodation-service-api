package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.prerequisite

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.completion.CrsCompletionRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.DtrExpiredReferralRule

@Component
class Cas3PrerequisiteRuleSet(
  dtrExpiredReferral: DtrExpiredReferralRule,
  crsCompletionSet: CrsCompletionRuleSet,
) : RuleSet {
  private val rules: List<Rule> = listOf(dtrExpiredReferral) + crsCompletionSet.getRules()

  override fun getRules(): List<Rule> = rules
}
