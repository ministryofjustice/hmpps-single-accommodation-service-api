package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.CrsSubmittedRuleMale
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.CrsSubmittedRuleNonMale
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.DtrExpiredRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.Cas3PrerequisiteData

@Component
class Cas3PrerequisiteRuleSet(
  dtrExpired: DtrExpiredRule,
  crsSubmittedRuleMale: CrsSubmittedRuleMale,
  crsSubmittedRuleNonMale: CrsSubmittedRuleNonMale,
) : RuleSet<Cas3PrerequisiteData> {
  private val rules: List<Rule<Cas3PrerequisiteData>> = listOf(dtrExpired, crsSubmittedRuleMale, crsSubmittedRuleNonMale)
  override val name: String = "Prerequisite"
  override fun getRules(): List<Rule<Cas3PrerequisiteData>> = rules
}
