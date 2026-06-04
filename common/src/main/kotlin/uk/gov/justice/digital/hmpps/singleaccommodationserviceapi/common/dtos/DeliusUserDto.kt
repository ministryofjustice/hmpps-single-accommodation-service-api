package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos

data class DeliusUserDto(
  val name: String,
  val username: String? = null,
  val staffCode: String? = null,
)
