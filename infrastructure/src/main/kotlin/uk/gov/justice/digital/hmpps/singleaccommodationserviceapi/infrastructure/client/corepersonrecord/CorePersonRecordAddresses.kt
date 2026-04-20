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

enum class AddressUsageCode(
  val description: String,
) {
  A02("Approved Premises"),
  A16("Awaiting Assessment"),
  A10("CAS2/BASS accommodation 13 weeks or more"),
  A11("CAS2/BASS accommodation less than 13 weeks"),
  A17("CAS3"),
  A07B("Friends/Family (settled)"),
  A07A("Friends/Family (transient)"),
  A14("HOIE Section 10"),
  A13("HOIE Section 4"),
  A08A("Homeless - Rough Sleeping"),
  A08C("Homeless - Shelter/Emergency Hostel/Campsite"),
  A08("Homeless - Squat"),
  A01A("Householder (Owner - freehold or leasehold)"),
  A15("Immigration Detention"),
  A12("Long Term Residential Healthcare"),
  A01C("Rental accommodation - private rental"),
  A01D("Rental accommodation - social rental (LA or other)"),
  A04("Supported Housing"),
  A03("Transient/short term accommodation"),
}

enum class AddressStatus(
  val description: String,
) {
  B("Bail"),
  M("Main"),
  MA("Postal"),
  P("Previous"),
  PR("Proposed"),
  PR1("Proposed for Resettlement"),
  RJ("Rejected"),
  RT("ROTL"),
  S("Secondary"),
}
