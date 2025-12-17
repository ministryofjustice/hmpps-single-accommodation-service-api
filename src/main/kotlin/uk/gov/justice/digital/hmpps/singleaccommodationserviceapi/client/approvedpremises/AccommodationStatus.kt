package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises

import java.time.LocalDate

data class AccommodationDetails(
  val type: AccommodationType,
  val subType: AccommodationSubType?,
  val name: String?,
  val isSettled: Boolean?,
  val offenderReleaseType: OffenderReleaseType?,
  val startDate: LocalDate?,
  val endDate: LocalDate?,
  val address: AddressDetails?,
)

enum class AccommodationType {
  PRISON,
  CAS1,
  CAS2,
  CAS2V2,
  CAS3,
  PRIVATE,
  NO_FIXED_ABODE,
}

enum class AccommodationSubType {
  OWNED,
  RENTED,
  LODGING,
}

// TODO get the proper business / domain name.
enum class OffenderReleaseType {
  REMAND,
  LICENCE,
  BAIL,
}

data class AddressDetails(
  val line1: String,
  val line2: String?,
  val region: String?,
  val city: String,
  val postCode: String,
)
