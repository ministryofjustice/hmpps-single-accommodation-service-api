package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.rules

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.enums.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.enums.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus

@Component
class ApplicationCompletionRule : Cas1CompletionRule {
  override val description = "FAIL if application is not complete"

  override fun evaluate(data: DomainData): RuleResult {
    val completedPlacementStatuses = listOf(
      Cas1PlacementStatus.UPCOMING,
      Cas1PlacementStatus.ARRIVED,
    )

    val isCompleteApplication = data.cas1Application?.applicationStatus == Cas1ApplicationStatus.PLACEMENT_ALLOCATED
    val isCompletePlacement = completedPlacementStatuses.contains(data.cas1Application?.placementStatus)

    val ruleStatus = if (isCompleteApplication && isCompletePlacement) RuleStatus.PASS else RuleStatus.FAIL

    return RuleResult(
      description = description,
      ruleStatus = ruleStatus,
    )
  }
}
