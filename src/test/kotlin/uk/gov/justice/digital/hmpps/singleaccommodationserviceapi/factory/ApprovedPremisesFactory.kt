package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.factory

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.AccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.AccommodationType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.CurrentAccommodationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.NextAccommodationDto
import java.time.LocalDate

fun buildCurrentAccommodationDto(
  type: AccommodationType = AccommodationType.CAS1_MOCK,
  endDate: LocalDate = LocalDate.now().plusDays(10),
) = CurrentAccommodationDto(
  type = type,
  endDate = endDate,
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
