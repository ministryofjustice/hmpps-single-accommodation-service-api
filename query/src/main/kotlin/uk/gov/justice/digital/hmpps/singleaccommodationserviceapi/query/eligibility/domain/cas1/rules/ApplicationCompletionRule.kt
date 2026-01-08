package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.rules

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.RuleAction
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.enums.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.enums.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus

@Component
class ApplicationCompletionRule : Cas1CompletionRule {
  override val description = "FAIL if application is not complete"
  override val actionable = true

  override fun buildAction(data: DomainData) = when (data.cas1Application?.applicationStatus) {
    Cas1ApplicationStatus.PLACEMENT_ALLOCATED
      -> when (data.cas1Application.placementStatus) {

      Cas1PlacementStatus.UPCOMING,
      Cas1PlacementStatus.ARRIVED
        -> error("No action needed")

      Cas1PlacementStatus.DEPARTED,
      Cas1PlacementStatus.NOT_ARRIVED,
      Cas1PlacementStatus.CANCELLED
        -> RuleAction("Create Placement")

      null -> error("Null Placement Status for ${data.cas1Application.applicationStatus} ${data.cas1Application.id}")
    }

    Cas1ApplicationStatus.AWAITING_PLACEMENT,
    Cas1ApplicationStatus.PENDING_PLACEMENT_REQUEST
      -> RuleAction("Create Placement")

    Cas1ApplicationStatus.AWAITING_ASSESSMENT,
    Cas1ApplicationStatus.UNALLOCATED_ASSESSMENT,
    Cas1ApplicationStatus.ASSESSMENT_IN_PROGRESS,
      -> RuleAction("Await Assessment", true)

    Cas1ApplicationStatus.REQUEST_FOR_FURTHER_INFORMATION
      -> RuleAction("Provide Information")

    else -> null
  }

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
      actionable = actionable,
      potentialAction = this.actionWrapper(ruleStatus, data),
    )
  }
}
