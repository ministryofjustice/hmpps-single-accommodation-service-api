package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos

import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class AccommodationDto(
    val crn: String,
    val current: AccommodationDetail,
    val next: AccommodationDetail,
)

data class AccommodationDetail(
  val id: UUID,
  val name: String?,
  val arrangementType: AccommodationArrangementType,
  val arrangementSubType: AccommodationArrangementSubType?,
  val arrangementSubTypeDescription: String?,
  val settledType: AccommodationSettledType,
  val offenderReleaseType: OffenderReleaseType?,
  val status: AccommodationStatus?,
  val address: AccommodationAddressDetails,
  val startDate: LocalDate?,
  val endDate: LocalDate?,
  val createdAt: Instant,
)

enum class AccommodationArrangementType {
  PRISON,
  CAS1,
  CAS2,
  CAS2V2,
  CAS3,
  PRIVATE,
  NO_FIXED_ABODE,
}

enum class AccommodationArrangementSubType {
  FRIENDS_OR_FAMILY,
  SOCIAL_RENTED,
  PRIVATE_RENTED_WHOLE_PROPERTY,
  PRIVATE_RENTED_ROOM,
  OWNED,
  OTHER,
}

enum class AccommodationSettledType {
  SETTLED,
  TRANSIENT,
}

enum class AccommodationStatus {
  NOT_CHECKED_YET,
  CHECKS_PASSED,
  CHECKS_FAILED,
  CONFIRMED
}

enum class OffenderReleaseType {
  REMAND,
  LICENCE,
  BAIL,
}

data class AccommodationAddressDetails(
  val postcode: String?,
  val subBuildingName: String?,
  val buildingName: String?,
  val buildingNumber: String?,
  val thoroughfareName: String?,
  val dependentLocality: String?,
  val postTown: String?,
  val county: String?,
  val country: String?,
  val uprn: String?,
)
