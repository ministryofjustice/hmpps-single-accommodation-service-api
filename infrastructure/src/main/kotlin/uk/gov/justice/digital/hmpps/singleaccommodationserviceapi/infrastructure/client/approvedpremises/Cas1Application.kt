package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.enums.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.enums.Cas1PlacementStatus
import java.util.UUID

data class Cas1Application(
  val id: UUID,
  val applicationStatus: Cas1ApplicationStatus,
  val placementStatus: Cas1PlacementStatus?,
)
