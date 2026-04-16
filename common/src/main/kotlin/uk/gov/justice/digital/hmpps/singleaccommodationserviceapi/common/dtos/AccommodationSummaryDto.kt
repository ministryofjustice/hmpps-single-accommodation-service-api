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
  val code: String,
  val description: String,
) {
  B("B", "Bail"),
  M("M", "Main"),
  MA("MA", "Postal"),
  P("P", "Previous"),
  PR("PR", "Proposed"),
  PR1("PR1", "Proposed for Resettlement"),
  RJ("RJ", "Rejected"),
  RT("RT", "ROTL"),
  S("S", "Secondary"),
}

enum class AccommodationTypeCode(
  val code: String,
  val description: String,
) {
  A02("A02", "Approved Premises"),
  A16("A16", "Awaiting Assessment"),
  A10("A10", "CAS2/BASS accommodation 13 weeks or more"),
  A11("A11", "CAS2/BASS accommodation less than 13 weeks"),
  A17("A17", "CAS3"),
  A07B("A07B", "Friends/Family (settled)"),
  A07A("A07A", "Friends/Family (transient)"),
  A14("A14", "HOIE Section 10"),
  A13("A13", "HOIE Section 4"),
  A08A("A08A", "Homeless - Rough Sleeping"),
  A08C("A08C", "Homeless - Shelter/Emergency Hostel/Campsite"),
  A08("A08", "Homeless - Squat"),
  A01A("A01A", "Householder (Owner - freehold or leasehold)"),
  A15("A15", "Immigration Detention"),
  A12("A12", "Long Term Residential Healthcare"),
  A01C("A01C", "Rental accommodation - private rental"),
  A01D("A01D", "Rental accommodation - social rental (LA or other)"),
  A04("A04", "Supported Housing"),
  A03("A03", "Transient/short term accommodation"),
}
