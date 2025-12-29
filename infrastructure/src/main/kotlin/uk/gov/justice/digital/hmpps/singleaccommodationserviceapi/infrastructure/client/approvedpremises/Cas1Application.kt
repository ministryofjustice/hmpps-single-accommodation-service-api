package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.enums.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.enums.Cas1PlacementStatus
import java.util.UUID

data class Cas1Application(
  val id: UUID,
  val applicationStatus: Cas1ApplicationStatus,
  val placementStatus: Cas1PlacementStatus?,
) {


  fun buildActions() = if (applicationStatus != Cas1ApplicationStatus.PLACEMENT_ALLOCATED ||
    placementStatus in setOf(
      Cas1PlacementStatus.CANCELLED,
      Cas1PlacementStatus.DEPARTED,
      Cas1PlacementStatus.NOT_ARRIVED,
    )
  ) {
    listOf("Create a placement request.")
  } else {
    emptyList()
  }
}
