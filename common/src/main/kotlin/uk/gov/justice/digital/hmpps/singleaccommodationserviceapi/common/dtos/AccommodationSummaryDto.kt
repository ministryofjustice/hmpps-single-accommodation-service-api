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
  val code: String,
  val description: String?,
)

data class AccommodationTypeDto(
  val code: String,
  val description: String?,
)

data class AccommodationAddressDetails(
  val postcode: String?,
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
