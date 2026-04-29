package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.completion

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1RequestForPlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityKeys
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext

@Component
class Cas1CompletionContextUpdater : ContextUpdater {

  override fun update(context: EvaluationContext): EvaluationContext {
    val updatedServiceResult = toServiceResult(context.data)

    return context.copy(currentResult = updatedServiceResult)
  }

  private fun toServiceResult(data: DomainData): ServiceResult {
    val applicationStatus = data.cas1Application?.applicationStatus
    val requestForPlacementStatus = data.cas1Application?.requestForPlacementStatus
    val placementStatus = data.cas1Application?.placementStatus

    return if (requestForPlacementStatus == null && placementStatus == null) {
      toServiceResultPriorToPlacementRequest(
        applicationStatus = applicationStatus,
      )
    } else {
      if (placementStatus == null) {
        toServiceResultBeforePlacement(
          applicationStatus = applicationStatus,
          requestForPlacementStatus = requestForPlacementStatus,
        )
      } else {
        toServiceResultAfterPlacement(
          applicationStatus = applicationStatus,
          requestForPlacementStatus = requestForPlacementStatus,
          placementStatus = placementStatus,
        )
      }
    }
  }

  private fun toServiceResultAfterPlacement(applicationStatus: Cas1ApplicationStatus?, requestForPlacementStatus: Cas1RequestForPlacementStatus?, placementStatus: Cas1PlacementStatus?) = when (applicationStatus) {
    Cas1ApplicationStatus.AWAITING_PLACEMENT ->
      if (
        requestForPlacementStatus == Cas1RequestForPlacementStatus.AWAITING_MATCH &&
        placementStatus == Cas1PlacementStatus.CANCELLED
      ) {
        ServiceResult(
          serviceStatus = ServiceStatus.PLACEMENT_CANCELLED,
          action = EligibilityKeys.CREATE_PLACEMENT,
          link = EligibilityKeys.CREATE_NEW_PLACEMENT_REQUEST,
        )
      } else {
        ServiceResult(serviceStatus = ServiceStatus.NOT_ELIGIBLE)
      }

    Cas1ApplicationStatus.PLACEMENT_ALLOCATED -> if (
      requestForPlacementStatus == Cas1RequestForPlacementStatus.PLACEMENT_BOOKED
    ) {
      when (placementStatus) {
        Cas1PlacementStatus.ARRIVED -> ServiceResult(
          serviceStatus = ServiceStatus.ARRIVED,
          action = null,
          link = EligibilityKeys.VIEW_APPLICATION,
        )

        Cas1PlacementStatus.DEPARTED -> ServiceResult(
          serviceStatus = ServiceStatus.PLACEMENT_REQUEST_NOT_STARTED,
          action = EligibilityKeys.CREATE_PLACEMENT,
          link = EligibilityKeys.CREATE_NEW_PLACEMENT_REQUEST,
        )

        Cas1PlacementStatus.NOT_ARRIVED -> ServiceResult(
          serviceStatus = ServiceStatus.NOT_ARRIVED,
          action = EligibilityKeys.CREATE_PLACEMENT,
          link = EligibilityKeys.CREATE_NEW_PLACEMENT_REQUEST,
        )

        else -> ServiceResult(serviceStatus = ServiceStatus.NOT_ELIGIBLE)
      }
    } else {
      ServiceResult(serviceStatus = ServiceStatus.NOT_ELIGIBLE)
    }

    else -> ServiceResult(serviceStatus = ServiceStatus.NOT_ELIGIBLE)
  }

  private fun toServiceResultBeforePlacement(applicationStatus: Cas1ApplicationStatus?, requestForPlacementStatus: Cas1RequestForPlacementStatus?) = when (applicationStatus) {
    Cas1ApplicationStatus.AWAITING_PLACEMENT -> when (requestForPlacementStatus) {
      Cas1RequestForPlacementStatus.AWAITING_MATCH -> ServiceResult(
        serviceStatus = ServiceStatus.PLACEMENT_REQUEST_SUBMITTED,
        action = EligibilityKeys.WAIT_FOR_PLACEMENT_REQUEST_RESULT,
        link = EligibilityKeys.VIEW_APPLICATION,
      )

      Cas1RequestForPlacementStatus.REQUEST_WITHDRAWN -> ServiceResult(
        serviceStatus = ServiceStatus.PLACEMENT_REQUEST_WITHDRAWN,
        action = EligibilityKeys.CREATE_PLACEMENT,
        link = EligibilityKeys.CREATE_NEW_PLACEMENT_REQUEST,
      )

      else -> ServiceResult(serviceStatus = ServiceStatus.NOT_ELIGIBLE)
    }

    Cas1ApplicationStatus.PENDING_PLACEMENT_REQUEST -> when (requestForPlacementStatus) {
      Cas1RequestForPlacementStatus.REQUEST_UNSUBMITTED -> ServiceResult(
        serviceStatus = ServiceStatus.PLACEMENT_REQUEST_NOT_STARTED,
        action = EligibilityKeys.CREATE_PLACEMENT,
        link = EligibilityKeys.CREATE_PLACEMENT_REQUEST,
      )

      Cas1RequestForPlacementStatus.REQUEST_REJECTED -> ServiceResult(
        serviceStatus = ServiceStatus.PLACEMENT_REQUEST_REJECTED,
        action = EligibilityKeys.CREATE_PLACEMENT,
        link = EligibilityKeys.CREATE_NEW_PLACEMENT_REQUEST,
      )

      Cas1RequestForPlacementStatus.AWAITING_MATCH,
      Cas1RequestForPlacementStatus.REQUEST_SUBMITTED,
      -> ServiceResult(
        serviceStatus = ServiceStatus.PLACEMENT_REQUEST_SUBMITTED,
        action = EligibilityKeys.WAIT_FOR_PLACEMENT_REQUEST_RESULT,
        link = EligibilityKeys.VIEW_APPLICATION,
      )

      Cas1RequestForPlacementStatus.REQUEST_WITHDRAWN -> ServiceResult(
        serviceStatus = ServiceStatus.PLACEMENT_REQUEST_WITHDRAWN,
        action = EligibilityKeys.CREATE_PLACEMENT,
        link = EligibilityKeys.CREATE_NEW_PLACEMENT_REQUEST,
      )

      else -> ServiceResult(serviceStatus = ServiceStatus.NOT_ELIGIBLE)
    }

    else -> ServiceResult(serviceStatus = ServiceStatus.NOT_ELIGIBLE)
  }

  private fun toServiceResultPriorToPlacementRequest(applicationStatus: Cas1ApplicationStatus?) = when (applicationStatus) {
    Cas1ApplicationStatus.AWAITING_ASSESSMENT,
    Cas1ApplicationStatus.UNALLOCATED_ASSESSMENT,
    Cas1ApplicationStatus.ASSESSMENT_IN_PROGRESS,
    -> ServiceResult(
      serviceStatus = ServiceStatus.SUBMITTED,
      action = EligibilityKeys.WAIT_FOR_ASSESSMENT_RESULT,
      link = EligibilityKeys.VIEW_APPLICATION,
    )

    Cas1ApplicationStatus.REQUEST_FOR_FURTHER_INFORMATION -> ServiceResult(
      serviceStatus = ServiceStatus.INFO_REQUESTED,
      action = EligibilityKeys.PROVIDE_INFORMATION,
      link = EligibilityKeys.VIEW_APPLICATION,
    )

    else -> ServiceResult(serviceStatus = ServiceStatus.NOT_ELIGIBLE)
  }
}
