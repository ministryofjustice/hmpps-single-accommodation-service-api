package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.accommodationdatadomain

import java.util.UUID

data class Accommodation(
  val id: UUID,
  val type: AccommodationType,
  val settledType: String,
  val status: String,
  val address: Address,
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

data class Address(
  val postcode: String,
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

data class DutyToRefer(
  val id: UUID,
  val status: String,
)

data class Crs(
  val id: UUID,
  val status: String,
)