package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.factory

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.AccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.AccommodationType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.Cas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.CurrentAccommodationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.NextAccommodationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.enums.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.enums.Cas1PlacementStatus
import java.time.LocalDate
import java.util.UUID

fun buildCurrentAccommodationDto(
  type: AccommodationType = AccommodationType.CAS1_MOCK,
  endDate: LocalDate = LocalDate.now().plusDays(10),
) = CurrentAccommodationDto(
  type = type,
  endDate = endDate,
)

fun buildCas1Application(
  id: UUID = UUID.randomUUID(),
  applicationStatus: Cas1ApplicationStatus = Cas1ApplicationStatus.AWAITING_ASSESSMENT,
  placementStatus: Cas1PlacementStatus? = null,
) = Cas1Application(
  id = id,
  applicationStatus = applicationStatus,
  placementStatus = placementStatus,
)

fun buildNextAccommodationDto(
  type: AccommodationType = AccommodationType.PRIVATE_ADDRESS_MOCK,
  startDate: LocalDate = LocalDate.now().plusDays(100),
) = NextAccommodationDto(
  type = type,
  startDate = startDate,
)

fun buildAccommodationStatus(
  currentAccommodationDto: CurrentAccommodationDto = buildCurrentAccommodationDto(),
  nextAccommodationDto: NextAccommodationDto = buildNextAccommodationDto(),
) = AccommodationStatus(currentAccommodationDto, nextAccommodationDto)
