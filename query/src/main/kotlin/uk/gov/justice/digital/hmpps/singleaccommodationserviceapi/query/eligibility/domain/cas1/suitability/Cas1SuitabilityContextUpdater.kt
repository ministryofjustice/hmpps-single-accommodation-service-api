package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.suitability

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseAction
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseActionType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.LinkType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityKeys
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext

@Component
class Cas1SuitabilityContextUpdater : ContextUpdater() {

  override val description = set("status from application")

  val notSubmitted = "notSubmitted"
  val applicationRejected = "applicationRejected"
  val notStarted = "notStarted"

  override val outcomes = mapOf(
    notSubmitted to ServiceResult(
      serviceStatus = ServiceStatus.NOT_SUBMITTED,
      action = CaseAction(type = CaseActionType.CONTINUE_APPROVED_PREMISE_APPLICATION, service = AccommodationService.CAS1),
      link = EligibilityKeys.CONTINUE_APPLICATION,
      linkType = LinkType.CAS1_VIEW_APPLICATION,
    ),
    applicationRejected to ServiceResult(
      serviceStatus = ServiceStatus.APPLICATION_REJECTED,
      action = CaseAction(type = CaseActionType.START_APPROVED_PREMISE_APPLICATION, service = AccommodationService.CAS1),
      link = EligibilityKeys.START_NEW_APPLICATION,
      linkType = LinkType.CAS1_START_APPLICATION,
    ),
    notStarted to ServiceResult(
      serviceStatus = ServiceStatus.NOT_STARTED,
      action = CaseAction(type = CaseActionType.START_APPROVED_PREMISE_APPLICATION, service = AccommodationService.CAS1),
      link = EligibilityKeys.START_APPLICATION,
      linkType = LinkType.CAS1_START_APPLICATION,
    ),
  )

  override fun toServiceResult(context: EvaluationContext): ServiceResult {
    val applicationStatus = context.data.cas1Application?.application?.status

    return when (applicationStatus) {
      Cas1ApplicationStatus.STARTED -> outcome(notSubmitted)
      Cas1ApplicationStatus.REJECTED -> outcome(applicationRejected)
      else -> outcome(notStarted)
    }
  }
}
