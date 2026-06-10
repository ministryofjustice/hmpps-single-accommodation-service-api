package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos

data class PageMetadata(
  val size: Long,
  val number: Long,
  val totalElements: Long,
  val totalPages: Long,
)
