package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleStatus

@Component
class NoUpcomingPlacementRule : Rule {
  override val description = "FAIL if no placement booked"

  override fun evaluate(data: DomainData): RuleResult {
    val isPass = data.cas1Application?.placement?.status == Cas1PlacementStatus.UPCOMING

    return RuleResult(
      description = description,
      ruleStatus = if (isPass) RuleStatus.PASS else RuleStatus.FAIL,
    )
  }
}
