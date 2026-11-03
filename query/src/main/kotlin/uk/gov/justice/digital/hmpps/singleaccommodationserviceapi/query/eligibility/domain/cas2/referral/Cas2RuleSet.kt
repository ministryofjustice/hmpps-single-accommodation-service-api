package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.referral

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.referral.rules.Cas2NotStartedRule

@Component
class Cas2RuleSet(
  cas2NotStarted: Cas2NotStartedRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(cas2NotStarted)
  override val name: String = "CAS2 Referral"
  override fun getRules(): List<Rule> = rules
}
