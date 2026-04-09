package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1RequestForPlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.ActionKeys
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.LinkKeys
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import java.time.Clock
import java.time.LocalDate
import java.time.temporal.ChronoUnit.DAYS
import java.util.UUID

object Cas1ServiceResultTransformer {
  fun toCas1ServiceResult(data: DomainData, clock: Clock): ServiceResult {
    val applicationStatus = data.cas1Application?.applicationStatus
    val requestForPlacementStatus = data.cas1Application?.requestForPlacementStatus
    val placementStatus = data.cas1Application?.placementStatus
    val suitableApplicationId = data.cas1Application?.id
    val releaseDate = data.releaseDate

    val notEligible = ServiceResult(
      serviceStatus = ServiceStatus.NOT_ELIGIBLE,
      suitableApplicationId = suitableApplicationId,
      action = null,
      link = null,
    )

    if (releaseDate == null) {
      return notEligible
    }

    val today = LocalDate.now(clock)
    val isWithinOneYear = !releaseDate.isAfter(today.plusYears(1))

    if (!isWithinOneYear) {
      return ServiceResult(
        serviceStatus = ServiceStatus.UPCOMING,
        suitableApplicationId = suitableApplicationId,
        action = if (applicationStatus == Cas1ApplicationStatus.STARTED) null else buildStartApprovedPremiseReferralAction(releaseDate, today),
        link = null,
      )
    }

    val isBeforeRequestForPlacement = requestForPlacementStatus == null && placementStatus == null

    return if (isBeforeRequestForPlacement) {
      toServiceResultPriorToPlacementRequest(
        applicationStatus = applicationStatus,
        suitableApplicationId = suitableApplicationId,
        notEligible = notEligible,
      )
    } else {
      val isBeforePlacement = placementStatus == null
      if (isBeforePlacement) {
        toServiceResultBeforePlacement(
          applicationStatus = applicationStatus,
          suitableApplicationId = suitableApplicationId,
          notEligible = notEligible,
          requestForPlacementStatus = requestForPlacementStatus,
        )
      } else {
        toServiceResultAfterPlacement(
          applicationStatus = applicationStatus,
          suitableApplicationId = suitableApplicationId,
          notEligible = notEligible,
          requestForPlacementStatus = requestForPlacementStatus,
          placementStatus = placementStatus,
        )
      }
    }
  }

  private fun toServiceResultAfterPlacement(applicationStatus: Cas1ApplicationStatus?, requestForPlacementStatus: Cas1RequestForPlacementStatus?, placementStatus: Cas1PlacementStatus?, suitableApplicationId: UUID?, notEligible: ServiceResult) = when (applicationStatus) {
    Cas1ApplicationStatus.AWAITING_PLACEMENT ->
      if (
        requestForPlacementStatus == Cas1RequestForPlacementStatus.AWAITING_MATCH &&
        placementStatus == Cas1PlacementStatus.CANCELLED
      ) {
        ServiceResult(
          serviceStatus = ServiceStatus.PLACEMENT_CANCELLED,
          suitableApplicationId = suitableApplicationId,
          action = ActionKeys.CREATE_PLACEMENT,
          link = LinkKeys.CREATE_NEW_PLACEMENT_REQUEST,
        )
      } else {
        notEligible
      }

    Cas1ApplicationStatus.PLACEMENT_ALLOCATED -> if (
      requestForPlacementStatus == Cas1RequestForPlacementStatus.PLACEMENT_BOOKED
    ) {
      when (placementStatus) {
        Cas1PlacementStatus.ARRIVED -> ServiceResult(
          serviceStatus = ServiceStatus.ARRIVED,
          suitableApplicationId = suitableApplicationId,
          action = null,
          link = LinkKeys.VIEW_APPLICATION,
        )

        Cas1PlacementStatus.UPCOMING -> notEligible
        Cas1PlacementStatus.DEPARTED -> ServiceResult(
          serviceStatus = ServiceStatus.PLACEMENT_REQUEST_NOT_STARTED,
          suitableApplicationId = suitableApplicationId,
          action = ActionKeys.CREATE_PLACEMENT,
          link = LinkKeys.CREATE_NEW_PLACEMENT_REQUEST,
        )

        Cas1PlacementStatus.NOT_ARRIVED -> ServiceResult(
          serviceStatus = ServiceStatus.NOT_ARRIVED,
          suitableApplicationId = suitableApplicationId,
          action = ActionKeys.CREATE_PLACEMENT,
          link = LinkKeys.CREATE_NEW_PLACEMENT_REQUEST,
        )

        else -> notEligible
      }
    } else {
      notEligible
    }

    else -> notEligible
  }

  private fun toServiceResultBeforePlacement(applicationStatus: Cas1ApplicationStatus?, requestForPlacementStatus: Cas1RequestForPlacementStatus?, suitableApplicationId: UUID?, notEligible: ServiceResult) = when (applicationStatus) {
    Cas1ApplicationStatus.AWAITING_PLACEMENT -> when (requestForPlacementStatus) {
      Cas1RequestForPlacementStatus.AWAITING_MATCH -> ServiceResult(
        serviceStatus = ServiceStatus.PLACEMENT_REQUEST_SUBMITTED,
        suitableApplicationId = suitableApplicationId,
        action = ActionKeys.WAIT_FOR_PLACEMENT_REQUEST_RESULT,
        link = LinkKeys.VIEW_APPLICATION,
      )

      Cas1RequestForPlacementStatus.REQUEST_WITHDRAWN -> ServiceResult(
        serviceStatus = ServiceStatus.PLACEMENT_REQUEST_WITHDRAWN,
        suitableApplicationId = suitableApplicationId,
        action = ActionKeys.CREATE_PLACEMENT,
        link = LinkKeys.CREATE_NEW_PLACEMENT_REQUEST,
      )

      else -> notEligible
    }

    Cas1ApplicationStatus.PENDING_PLACEMENT_REQUEST -> when (requestForPlacementStatus) {
      Cas1RequestForPlacementStatus.REQUEST_UNSUBMITTED -> ServiceResult(
        serviceStatus = ServiceStatus.PLACEMENT_REQUEST_NOT_STARTED,
        suitableApplicationId = suitableApplicationId,
        action = ActionKeys.CREATE_PLACEMENT,
        link = LinkKeys.CREATE_PLACEMENT_REQUEST,
      )

      Cas1RequestForPlacementStatus.REQUEST_REJECTED -> ServiceResult(
        serviceStatus = ServiceStatus.PLACEMENT_REQUEST_REJECTED,
        suitableApplicationId = suitableApplicationId,
        action = ActionKeys.CREATE_PLACEMENT,
        link = LinkKeys.CREATE_NEW_PLACEMENT_REQUEST,
      )

      Cas1RequestForPlacementStatus.AWAITING_MATCH,
      Cas1RequestForPlacementStatus.REQUEST_SUBMITTED,
      -> ServiceResult(
        serviceStatus = ServiceStatus.PLACEMENT_REQUEST_SUBMITTED,
        suitableApplicationId = suitableApplicationId,
        action = ActionKeys.WAIT_FOR_PLACEMENT_REQUEST_RESULT,
        link = LinkKeys.VIEW_APPLICATION,
      )

      Cas1RequestForPlacementStatus.REQUEST_WITHDRAWN -> ServiceResult(
        serviceStatus = ServiceStatus.PLACEMENT_REQUEST_WITHDRAWN,
        suitableApplicationId = suitableApplicationId,
        action = ActionKeys.CREATE_PLACEMENT,
        link = LinkKeys.CREATE_NEW_PLACEMENT_REQUEST,
      )

      else -> notEligible
    }

    else -> notEligible
  }

  private fun toServiceResultPriorToPlacementRequest(applicationStatus: Cas1ApplicationStatus?, suitableApplicationId: UUID?, notEligible: ServiceResult) = when (applicationStatus) {
    Cas1ApplicationStatus.AWAITING_ASSESSMENT,
    Cas1ApplicationStatus.UNALLOCATED_ASSESSMENT,
    Cas1ApplicationStatus.ASSESSMENT_IN_PROGRESS,
    -> ServiceResult(
      serviceStatus = ServiceStatus.SUBMITTED,
      suitableApplicationId = suitableApplicationId,
      action = ActionKeys.WAIT_FOR_ASSESSMENT_RESULT,
      link = LinkKeys.VIEW_APPLICATION,
    )

    Cas1ApplicationStatus.REQUEST_FOR_FURTHER_INFORMATION -> ServiceResult(
      serviceStatus = ServiceStatus.INFO_REQUESTED,
      suitableApplicationId = suitableApplicationId,
      action = ActionKeys.PROVIDE_INFORMATION,
      link = LinkKeys.VIEW_APPLICATION,
    )

    Cas1ApplicationStatus.STARTED -> ServiceResult(
      serviceStatus = ServiceStatus.NOT_SUBMITTED,
      suitableApplicationId = suitableApplicationId,
      action = ActionKeys.CONTINUE_APPROVED_PREMISE_APPLICATION,
      link = LinkKeys.CONTINUE_APPLICATION,
    )

    Cas1ApplicationStatus.REJECTED -> ServiceResult(
      serviceStatus = ServiceStatus.APPLICATION_REJECTED,
      suitableApplicationId = suitableApplicationId,
      action = ActionKeys.START_APPROVED_PREMISE_APPLICATION,
      link = LinkKeys.START_NEW_APPLICATION,
    )

    Cas1ApplicationStatus.WITHDRAWN,
    Cas1ApplicationStatus.INAPPLICABLE,
    Cas1ApplicationStatus.EXPIRED,
    null,
    -> ServiceResult(
      serviceStatus = ServiceStatus.NOT_STARTED,
      suitableApplicationId = suitableApplicationId,
      action = ActionKeys.START_APPROVED_PREMISE_APPLICATION,
      link = LinkKeys.START_APPLICATION,
    )

    else -> notEligible
  }

  private fun buildStartApprovedPremiseReferralAction(releaseDate: LocalDate, today: LocalDate): String {
    val dateToStartReferral = releaseDate.minusYears(1)
    val daysUntilReferralMustStart = DAYS.between(today, dateToStartReferral).toInt()

    return when {
      daysUntilReferralMustStart > 1
      -> "${ActionKeys.START_APPROVED_PREMISE_APPLICATION} in $daysUntilReferralMustStart days"

      daysUntilReferralMustStart < 1 -> ActionKeys.START_APPROVED_PREMISE_APPLICATION

      else -> "${ActionKeys.START_APPROVED_PREMISE_APPLICATION} in 1 day"
    }
  }
}
