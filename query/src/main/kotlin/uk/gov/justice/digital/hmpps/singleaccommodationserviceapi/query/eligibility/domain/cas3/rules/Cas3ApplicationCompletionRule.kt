package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.rules

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3AssessmentStatus
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
    val isCompletePlacement = data.cas3Application?.bookingStatus in completedPlacementStatuses
    val isCompleteAssessment = data.cas3Application?.assessmentStatus == Cas3AssessmentStatus.READY_TO_PLACE
    val isCompleteApplication = data.cas3Application?.applicationStatus == Cas3ApplicationStatus.SUBMITTED

    val ruleStatus = if (isCompletePlacement && isCompleteAssessment && isCompleteApplication) RuleStatus.PASS else RuleStatus.FAIL

    return RuleResult(
      description = description,
      ruleStatus = ruleStatus,
    )
  }
}
