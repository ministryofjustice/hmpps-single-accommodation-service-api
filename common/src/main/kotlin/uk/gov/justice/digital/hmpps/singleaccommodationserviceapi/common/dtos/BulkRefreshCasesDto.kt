package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos

data class BulkRefreshCasesByCrnCommand(
  val crns: List<String>,
  val dryRun: Boolean = true,
)

data class BulkRefreshCasesResultDto(
  val dryRun: Boolean,
  val crnsRequested: Int,
  val casesFound: Int,
  val refreshesRequested: Int,
  val crnsNotFound: List<String>,
)
