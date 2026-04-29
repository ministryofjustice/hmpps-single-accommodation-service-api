package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas1ApplicationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.Cas1RequestForPlacementStatus
import java.util.UUID

fun buildCas1ApplicationDto(
  id: UUID = UUID.randomUUID(),
  applicationStatus: Cas1ApplicationStatus = Cas1ApplicationStatus.AWAITING_ASSESSMENT,
  placementStatus: Cas1PlacementStatus? = null,
  requestForPlacementStatus: Cas1RequestForPlacementStatus? = null,
) = Cas1ApplicationDto(
  id = id,
  applicationStatus = applicationStatus,
  placementStatus = placementStatus,
  requestForPlacementStatus = requestForPlacementStatus,
)
