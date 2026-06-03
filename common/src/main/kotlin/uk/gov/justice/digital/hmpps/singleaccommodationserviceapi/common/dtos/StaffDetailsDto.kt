package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos

data class StaffDetailsDto(
  val forename: String,
  val surname: String,
  val username: String? = null,
  val staffCode: String? = null,
)
