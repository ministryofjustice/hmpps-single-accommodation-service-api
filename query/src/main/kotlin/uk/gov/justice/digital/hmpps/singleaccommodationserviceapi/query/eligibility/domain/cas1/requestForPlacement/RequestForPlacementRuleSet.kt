package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.requestForPlacement

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.requestForPlacement.rules.NoRequestForPlacementRule

@Component
class RequestForPlacementRuleSet(
  noRequestForPlacement: NoRequestForPlacementRule,
) : RuleSet {
  private val rules: List<Rule> = listOf(
    noRequestForPlacement,
  )

  override val name: String = "Request For Placement"

  override fun getRules(): List<Rule> = rules
}
