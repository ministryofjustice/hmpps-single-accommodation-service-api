package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.referral

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.PrioritisedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.referral.rules.NoLiveCrsRule

@Component
class LiveCrsRuleSet(
  crsSubmitted: NoLiveCrsRule,
) : RuleSet {
  private val rules: List<PrioritisedRule> = listOf(PrioritisedRule(crsSubmitted))
  override val name: String = "Live CRS"
  override fun getRules(): List<PrioritisedRule> = rules
}
