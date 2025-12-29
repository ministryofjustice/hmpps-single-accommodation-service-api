package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.privateaddress

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.AddressDetails
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
