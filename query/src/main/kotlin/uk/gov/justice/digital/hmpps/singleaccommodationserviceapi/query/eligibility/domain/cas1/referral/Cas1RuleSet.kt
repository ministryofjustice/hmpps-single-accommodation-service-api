package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.referral

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.PrioritisedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.referral.rules.Cas1ExpiredRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.referral.rules.Cas1NotActiveRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.referral.rules.Cas1NotPresentRule

@Component
class Cas1RuleSet(
  applicationPresent: Cas1NotPresentRule,
  applicationSuitability: Cas1NotActiveRule,
  applicationRelevantExpired: Cas1ExpiredRule,
) : RuleSet {
  private val rules: List<PrioritisedRule> = listOf(
    PrioritisedRule(applicationPresent),
    PrioritisedRule(applicationRelevantExpired),
    PrioritisedRule(applicationSuitability),
  )

  override val name: String = "Suitability"

  override fun getRules(): List<PrioritisedRule> = rules
}
