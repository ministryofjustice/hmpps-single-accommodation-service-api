package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos

data class ReplayFailedCaseRefreshResultDto(
  val refreshRequestsReplayed: Int,
  val refreshRequestsDeleted: Int,
)