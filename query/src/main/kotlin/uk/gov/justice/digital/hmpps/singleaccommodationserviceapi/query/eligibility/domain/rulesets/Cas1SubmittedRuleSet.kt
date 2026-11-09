package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas1PendingPlacementRequestRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class Cas1SubmittedRuleSet(
  cas1PendingPlacementRequest: Cas1PendingPlacementRequestRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(
    cas1PendingPlacementRequest,
  )

  override val name: String = "PASS if application is not pending a placement request"

  override fun getRules(): List<Rule> = rules
}
