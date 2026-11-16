package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas1ApplicationExpiredRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas1NoApplicationRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class Cas1ApplicationPresentAndNotExpiredRuleSet(
  noApplication: Cas1NoApplicationRule,
  applicationExpired: Cas1ApplicationExpiredRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(
    noApplication,
    applicationExpired,
  )

  override val name: String = "PASS if there is an unexpired CAS1 application"

  override fun getRules(): List<Rule> = rules
}
