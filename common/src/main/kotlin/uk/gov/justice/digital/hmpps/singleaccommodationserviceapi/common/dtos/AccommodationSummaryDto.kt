package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos

import java.time.LocalDate
import java.util.UUID

data class AccommodationSummaryDto(
  val crn: String,
  val startDate: LocalDate? = null,
  val endDate: LocalDate? = null,
  val address: AccommodationAddressDetails,
  val status: AccommodationStatusDto? = null,
  val type: AccommodationTypeDto? = null,
)

data class AccommodationDetailDto(
  val crn: String,
  val cprAddressId: UUID? = null,
  val startDate: LocalDate? = null,
  val endDate: LocalDate? = null,
  val address: AccommodationAddressDetails,
  val status: AccommodationStatusDto? = null,
  val type: AccommodationTypeDto? = null,
  val typeVerified: Boolean? = null,
  val noFixedAbode: Boolean? = null,
)

data class AccommodationStatusDto(
  val code: String,
  val description: String? = null,
)

data class AccommodationTypeDto(
  val code: String,
  val description: String? = null,
)

data class AccommodationAddressDetails(
  val postcode: String? = null,
  val subBuildingName: String? = null,
  val buildingName: String? = null,
  val buildingNumber: String? = null,
  val thoroughfareName: String? = null,
  val dependentLocality: String? = null,
  val postTown: String? = null,
  val county: String? = null,
  val country: String? = null,
  val uprn: String? = null,
)
