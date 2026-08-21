package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.bulkload.json

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
