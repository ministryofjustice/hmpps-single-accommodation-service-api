package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos

data class ArchiveCasesByCrnCommand(
  val crns: List<String>,
  val dryRun: Boolean = true,
)

data class ArchiveCasesResultDto(
  val dryRun: Boolean,
  val crnsRequested: Int,
  val casesFound: Int,
  val casesArchived: Int,
  val refreshRequestsDeleted: Int,
  val crnsNotFound: List<String>,
)
