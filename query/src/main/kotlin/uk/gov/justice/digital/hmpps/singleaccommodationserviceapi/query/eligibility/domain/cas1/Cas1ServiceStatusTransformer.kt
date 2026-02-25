package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.enums.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.enums.Cas1PlacementStatus

object Cas1ServiceStatusTransformer {
  fun toServiceStatus(cas1Application: Cas1Application?, isWithinOneYear: Boolean) =
    when (isWithinOneYear) {
      false -> ServiceStatus.UPCOMING
      true -> when (cas1Application?.applicationStatus) {
        Cas1ApplicationStatus.PLACEMENT_ALLOCATED,
          -> when (cas1Application.placementStatus) {
          Cas1PlacementStatus.ARRIVED -> ServiceStatus.PLACEMENT_BOOKED
          Cas1PlacementStatus.UPCOMING -> ServiceStatus.PLACEMENT_BOOKED
          Cas1PlacementStatus.DEPARTED -> ServiceStatus.PLACEMENT_BOOKED
          Cas1PlacementStatus.NOT_ARRIVED -> ServiceStatus.NOT_ARRIVED
          Cas1PlacementStatus.CANCELLED -> ServiceStatus.PLACEMENT_CANCELLED
          null -> ServiceStatus.PLACEMENT_BOOKED
        }

        Cas1ApplicationStatus.AWAITING_PLACEMENT,
        Cas1ApplicationStatus.PENDING_PLACEMENT_REQUEST,
          -> ServiceStatus.PLACEMENT_BOOKED

        Cas1ApplicationStatus.REQUEST_FOR_FURTHER_INFORMATION,
          -> ServiceStatus.INFO_REQUESTED

        Cas1ApplicationStatus.AWAITING_ASSESSMENT,
        Cas1ApplicationStatus.UNALLOCATED_ASSESSMENT,
        Cas1ApplicationStatus.ASSESSMENT_IN_PROGRESS,
          -> ServiceStatus.SUBMITTED

        Cas1ApplicationStatus.STARTED,
          -> ServiceStatus.NOT_SUBMITTED

        Cas1ApplicationStatus.REJECTED,
          -> ServiceStatus.REJECTED

        null,
        Cas1ApplicationStatus.WITHDRAWN,
        Cas1ApplicationStatus.EXPIRED,
        Cas1ApplicationStatus.INAPPLICABLE,
          -> ServiceStatus.NOT_STARTED
      }
    }
}
