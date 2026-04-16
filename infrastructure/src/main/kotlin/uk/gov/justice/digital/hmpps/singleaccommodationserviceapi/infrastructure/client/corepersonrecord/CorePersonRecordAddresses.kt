package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord

import java.time.LocalDate
import java.util.UUID

data class CorePersonRecordAddresses(
  val crn: String,
  val addresses: List<Address> = emptyList(),
)

data class Address(
  val cprAddressId: UUID? = null,
  val noFixedAbode: Boolean? = null,
  val startDate: LocalDate? = null,
  val endDate: LocalDate? = null,
  val postcode: String? = null,
  val subBuildingName: String? = null,
  val buildingName: String? = null,
  val buildingNumber: String? = null,
  val thoroughfareName: String? = null,
  val dependentLocality: String? = null,
  val postTown: String? = null,
  val county: String? = null,
  val country: String? = null,
  val countryCode: String? = null,
  val addressStatus: AddressStatus? = null,
  val addressUsage: AddressUsage? = null,
  val uprn: String? = null,
)

data class AddressUsage(
  val addressUsageCode: AddressUsageCode,
  val addressUsageDescription: String? = null,
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
