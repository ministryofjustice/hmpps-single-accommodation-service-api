package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos

data class BulkLoadCasesCommand(
  val teamCodes: List<String>,
  val dryRun: Boolean = true,
)

data class BulkLoadCasesResultDto(
  val dryRun: Boolean,
  val teamsProcessed: Int,
  val crnsFound: Int,
  val casesAlreadyPresent: Int,
  val casesCreated: Int,
  val refreshesRequested: Int,
  val errors: List<BulkLoadCasesErrorDto>,
)

data class BulkLoadCasesErrorDto(
  val teamCode: String,
  val message: String,
)
