package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.rules

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus

private val completedPlacementStatuses = listOf(
  Cas3PlacementStatus.PROVISIONAL,
  Cas3PlacementStatus.CONFIRMED,
  Cas3PlacementStatus.ARRIVED,
)

@Component
class Cas3ApplicationCompletionRule : Cas3CompletionRule {
  override val description = "FAIL if CAS3 application is not complete"

  override fun evaluate(data: DomainData): RuleResult {

    val isCompleteApplication = data.cas3Application?.applicationStatus == Cas3ApplicationStatus.PLACED
    val isCompletePlacement = data.cas3Application?.placementStatus in completedPlacementStatuses

    val ruleStatus = if (isCompleteApplication && isCompletePlacement) RuleStatus.PASS else RuleStatus.FAIL

    return RuleResult(
      description = description,
      ruleStatus = ruleStatus,
    )
  }
}
