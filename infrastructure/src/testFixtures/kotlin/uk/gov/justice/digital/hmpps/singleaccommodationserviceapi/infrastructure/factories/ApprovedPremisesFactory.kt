package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1RequestForPlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3AssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3BookingStatus
import java.util.UUID

fun buildCas1Application(
  id: UUID = UUID.randomUUID(),
  applicationStatus: Cas1ApplicationStatus = Cas1ApplicationStatus.AWAITING_ASSESSMENT,
  placementStatus: Cas1PlacementStatus? = null,
  requestForPlacementStatus: Cas1RequestForPlacementStatus? = null,
) = Cas1Application(
  id = id,
  applicationStatus = applicationStatus,
  placementStatus = placementStatus,
  requestForPlacementStatus = requestForPlacementStatus,
)

fun buildCas3Application(
  id: UUID = UUID.randomUUID(),
  applicationStatus: Cas3ApplicationStatus = Cas3ApplicationStatus.IN_PROGRESS,
  assessmentStatus: Cas3AssessmentStatus? = null,
  bookingStatus: Cas3BookingStatus? = null,
) = Cas3Application(
  id = id,
  applicationStatus = applicationStatus,
  bookingStatus = bookingStatus,
  assessmentStatus = assessmentStatus,
)
