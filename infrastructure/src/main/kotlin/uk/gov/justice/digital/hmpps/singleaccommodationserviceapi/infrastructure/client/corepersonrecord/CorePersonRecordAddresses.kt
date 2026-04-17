package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord

import java.time.LocalDate
import java.util.UUID

data class CorePersonRecordAddresses(
  val crn: String,
  val addresses: List<Address> = emptyList(),
)

data class Address(
  val cprAddressId: UUID?,
  val noFixedAbode: Boolean?,
  val startDate: LocalDate?,
  val endDate: LocalDate?,
  val postcode: String?,
  val subBuildingName: String?,
  val buildingName: String?,
  val buildingNumber: String?,
  val thoroughfareName: String?,
  val dependentLocality: String?,
  val postTown: String?,
  val county: String?,
  val country: String?,
  val countryCode: String?,
  val addressStatus: AddressStatus?,
  val addressUsage: AddressUsage?,
  val uprn: String?,
)

data class AddressUsage(
  val addressUsageCode: AddressUsageCode,
  val addressUsageDescription: String?,
)

enum class AddressUsageCode {
  A02,
  A16,
  A10,
  A11,
  A17,
  A07B,
  A07A,
  A14,
  A13,
  A08A,
  A08C,
  A08,
  A01A,
  A15,
  A12,
  A01C,
  A01D,
  A04,
  A03,
}

enum class AddressStatus {
  B,
  M,
  MA,
  P,
  PR,
  PR1,
  RJ,
  RT,
  S,
}
