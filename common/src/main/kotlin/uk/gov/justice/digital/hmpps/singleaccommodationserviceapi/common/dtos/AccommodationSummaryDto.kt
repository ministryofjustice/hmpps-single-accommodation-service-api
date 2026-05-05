package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos

import java.time.LocalDate

data class AccommodationSummaryDto(
  val crn: String,
  val startDate: LocalDate?,
  val endDate: LocalDate?,
  val address: AccommodationAddressDetails,
  val status: AccommodationStatusDto?,
  val type: AccommodationTypeDto?,
)

data class AccommodationStatusDto(
  val code: AccommodationStatusCode,
  val description: String,
)

data class AccommodationTypeDto(
  val code: AccommodationTypeCode,
  val description: String,
)

enum class AccommodationStatusCode(
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

enum class AccommodationTypeCode(
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
