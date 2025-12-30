package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos

import java.time.LocalDateTime

data class PrivateAddressesDto(
  val crn: String,
  val addresses: List<AddressDto>,
)

data class AddressDto(
  val id: String,
  val status: String,
  val address: AddressDetails,
  val addedBy: AddedBy,
  val addedDate: LocalDateTime,
)

data class AddedBy(
  val id: String,
  val name: String,
  val role: String,
)
