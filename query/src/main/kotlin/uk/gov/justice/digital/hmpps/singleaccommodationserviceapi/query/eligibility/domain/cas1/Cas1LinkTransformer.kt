package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.enums.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.enums.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData

object Cas1LinkTransformer {
  fun buildCas1Link(data: DomainData, isWithinOneYear: Boolean) =
    if (isWithinOneYear) {
      when (data.cas1Application?.applicationStatus) {
        null,
        Cas1ApplicationStatus.INAPPLICABLE,
        Cas1ApplicationStatus.WITHDRAWN,
        Cas1ApplicationStatus.EXPIRED,
          -> "Start application"

        Cas1ApplicationStatus.STARTED,
          -> "Continue application"

        Cas1ApplicationStatus.REJECTED,
          -> "Start new application"

        Cas1ApplicationStatus.PLACEMENT_ALLOCATED,
          -> when (data.cas1Application.placementStatus) {
          Cas1PlacementStatus.ARRIVED -> "View application"
          Cas1PlacementStatus.UPCOMING -> "View application"
          Cas1PlacementStatus.DEPARTED -> "Create new placement request"
          Cas1PlacementStatus.NOT_ARRIVED -> "Create new placement request"
          Cas1PlacementStatus.CANCELLED -> "Create new placement request"
          null -> "Create new placement request"
        }

        Cas1ApplicationStatus.UNALLOCATED_ASSESSMENT,
        Cas1ApplicationStatus.AWAITING_ASSESSMENT,
        Cas1ApplicationStatus.ASSESSMENT_IN_PROGRESS,
        Cas1ApplicationStatus.REQUEST_FOR_FURTHER_INFORMATION,
          -> "View application"

        Cas1ApplicationStatus.AWAITING_PLACEMENT,
        Cas1ApplicationStatus.PENDING_PLACEMENT_REQUEST,
          -> "Create new placement request"
      }
    } else {
      null
    }
}
