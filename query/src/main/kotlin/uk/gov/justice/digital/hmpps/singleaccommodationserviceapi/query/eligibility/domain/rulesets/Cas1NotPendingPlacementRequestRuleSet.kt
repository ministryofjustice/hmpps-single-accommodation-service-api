package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rulesets

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas1PendingPlacementRequestRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleSet

@Component
class Cas1NotPendingPlacementRequestRuleSet(
  pendingPlacementRequest: Cas1PendingPlacementRequestRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(
    pendingPlacementRequest,
  )

  override val name: String = "PASS if the CAS1 application status is not 'Pending Placement Request'"

  override fun getRules(): List<Rule> = rules
}
