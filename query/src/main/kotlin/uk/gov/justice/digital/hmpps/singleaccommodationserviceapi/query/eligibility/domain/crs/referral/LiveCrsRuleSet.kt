package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.referral

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.referral.rules.NoLiveCrsRule

@Component
class LiveCrsRuleSet(
  noLiveCrs: NoLiveCrsRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(noLiveCrs)
  override val name: String = "Live CRS"
  override fun getRules(): List<Rule> = rules
}
