package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.admin.json

fun bulkLoadCasesRequestBody(
  teamCodes: List<String>,
  dryRun: Boolean? = null,
): String {
  val teamCodesJson = teamCodes.joinToString(", ") { """"$it"""" }
  val dryRunJson = dryRun?.let { """, "dryRun" : $it""" } ?: ""

  return """
  {
    "teamCodes" : [$teamCodesJson]$dryRunJson
  }
  """.trimIndent()
}

fun bulkRefreshCasesByCrnRequestBody(
  crns: List<String>,
  dryRun: Boolean? = null,
): String {
  val crnsJson = crns.joinToString(", ") { """"$it"""" }
  val dryRunJson = dryRun?.let { """, "dryRun" : $it""" } ?: ""

  return """
  {
    "crns" : [$crnsJson]$dryRunJson
  }
  """.trimIndent()
}
