package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.prerequisite

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.PrioritisedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.prerequisite.rules.CrsSubmittedRuleMale
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.prerequisite.rules.CrsSubmittedRuleNonMale
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.rules.DtrExpiredRule

@Component
class Cas3PrerequisiteRuleSet(
  dtrExpired: DtrExpiredRule,
  crsSubmittedRuleMale: CrsSubmittedRuleMale,
  crsSubmittedRuleNonMale: CrsSubmittedRuleNonMale,
) : RuleSet {
  private val rules: List<PrioritisedRule> = listOf(PrioritisedRule(dtrExpired, 0), PrioritisedRule(crsSubmittedRuleMale, 0), PrioritisedRule(crsSubmittedRuleNonMale, 0))
  override val name: String = "Prerequisite"
  override fun getRules(): List<PrioritisedRule> = rules
}
