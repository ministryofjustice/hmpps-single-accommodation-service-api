package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises

import java.time.LocalDate

data class CurrentAccommodationDto(val type: AccommodationType?, val endDate: LocalDate?)

data class NextAccommodationDto(val type: AccommodationType?, val startDate: LocalDate?)

data class AccommodationStatus(
  val currentAccommodation: CurrentAccommodationDto,
  val nextAccommodation: NextAccommodationDto,
)

enum class AccommodationType {
  CAS1_MOCK,
  CAS2_MOCK,
  CAS3_MOCK,
  PRIVATE_ADDRESS_MOCK,
}
