package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.enums.Cas3ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.enums.Cas3PlacementStatus
import java.util.UUID

data class Cas3Application(
  val id: UUID,
  val applicationStatus: Cas3ApplicationStatus,
  val placementStatus: Cas3PlacementStatus?,
)