package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos

data class AddressLookupResponse(
  val addresses: List<Address>,
  val total: Int,
  val offset: Int,
  val maxResults: Int,
)

data class Address(
  val uprn: String?,
  val singleLine: String?,
  val subBuildingName: String?,
  val buildingName: String?,
  val buildingNumber: String?,
  val street: String?,
  val locality: String?,
  val town: String?,
  val county: String?,
  val country: String?,
  val postcode: String?,
  val classification: String?,
  val status: String?,
  val x: Double?,
  val y: Double?,
  val match: Double?,
  val matchDescription: String?,
)
