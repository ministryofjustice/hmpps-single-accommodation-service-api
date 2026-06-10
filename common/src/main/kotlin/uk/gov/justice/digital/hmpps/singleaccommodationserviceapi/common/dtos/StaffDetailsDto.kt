package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos

data class StaffDetailsDto(
  val name: String,
  val username: String? = null,
  val staffCode: String? = null,
)
