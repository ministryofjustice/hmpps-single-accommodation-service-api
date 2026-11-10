package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas1MoreInfoRequestedRequestRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.Cas1ApplicationData

@Component
class MoreInfoNotRequestedRuleSet(
  cas1MoreInfoRequestedRequest: Cas1MoreInfoRequestedRequestRule,
) : RuleSet<Cas1ApplicationData> {
  private val rules: List<Rule<Cas1ApplicationData>> = listOf(
    cas1MoreInfoRequestedRequest,
  )

  override val name: String = "PASS if more information has not been requested for referral"

  override fun getRules(): List<Rule<Cas1ApplicationData>> = rules
}
