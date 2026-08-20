package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.prerequisite

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.completion.CrsCompletionRuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.DtrExpiredReferralRule

@Component
class Cas3PrerequisiteRuleSet(
  dtrExpiredReferral: DtrExpiredReferralRule,
  @Qualifier("crsCompletionRuleSetMale") crsCompletionSetMale: CrsCompletionRuleSet,
  @Qualifier("crsCompletionRuleSetNonMale") crsCompletionSetNonMale: CrsCompletionRuleSet,
) : RuleSet {
  private val rules: List<Rule> = listOf(dtrExpiredReferral) + crsCompletionSetMale.getRules() + crsCompletionSetNonMale.getRules()
  override fun getRules(): List<Rule> = rules
}
