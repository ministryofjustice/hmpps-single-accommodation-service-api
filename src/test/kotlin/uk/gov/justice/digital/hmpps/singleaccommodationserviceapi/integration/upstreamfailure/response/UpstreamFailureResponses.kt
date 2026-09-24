package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.upstreamfailure.response

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.TestData

private fun caseJson(
  forename: String? = "First",
  middleNames: String? = "Middle",
  surname: String? = "Last",
  dateOfBirth: String? = "2000-12-03",
  crn: String = "FAKECRN1",
  prisonNumber: String? = "PRI1",
  tierScore: String? = "A1",
  riskLevel: String? = "VERY_HIGH",
  pncReference: String? = "Some PNC Reference",
  assignedTo: String = """{"forename":"First","surname":"Last","username":"user1"}""",
) = """
{
  "forename": ${if (forename != null) "\"$forename\"" else "null"},
  "middleNames": ${if (middleNames != null) "\"$middleNames\"" else "null"},
  "surname": ${if (surname != null) "\"$surname\"" else "null"},
  "dateOfBirth": ${if (dateOfBirth != null) "\"$dateOfBirth\"" else "null"},
  "crn": "$crn",
  "prisonNumber": ${if (prisonNumber != null) "\"$prisonNumber\"" else "null"},
  "photoUrl": null,
  "tierScore": ${if (tierScore != null) "\"$tierScore\"" else "null"},
  "riskLevel": ${if (riskLevel != null) "\"$riskLevel\"" else "null"},
  "pncReference": ${if (pncReference != null) "\"$pncReference\"" else "null"},
  "assignedTo": $assignedTo,
  "userAccess": "FULL",
  "limitedAccess": false,
  "accommodationSummaries": null
}
""".trimIndent()

private fun failureJson(
  endpoint: String,
  failureType: String,
  httpResponseStatus: String? = null,
  message: String,
  identifierCrn: String? = null,
) = """
{
  "endpoint": "$endpoint",
  "failureType": "$failureType",
  "httpResponseStatus": ${if (httpResponseStatus != null) "\"$httpResponseStatus\"" else "null"},
  "message": "$message",
  "identifier": ${if (identifierCrn != null) """{ "type": "CRN", "value": "$identifierCrn" }""" else "null"}
}
""".trimIndent()

private fun tierServerErrorFailure(upstreamUrl: String) = failureJson(
  endpoint = "getTierByCrn",
  failureType = "UPSTREAM_HTTP_ERROR",
  httpResponseStatus = "500 INTERNAL_SERVER_ERROR",
  message = "500 Internal Server Error from GET $upstreamUrl",
  identifierCrn = null,
)

private fun tierTimeoutFailure() = failureJson(
  endpoint = "getTierByCrn",
  failureType = "TIMEOUT",
  message = "Request timed out",
  identifierCrn = null,
)

@TestData
fun expectedSingleCrnTierServerError(crn: String, prisonNumber: String, tierUpstreamUrl: String) = """{ "data": ${caseJson(tierScore = null, crn = crn, prisonNumber = prisonNumber)}, 
  |"upstreamFailures": [${tierServerErrorFailure(tierUpstreamUrl)}] }
""".trimMargin()

@TestData
fun expectedSingleCrnTierTimeout(crn: String, prisonNumber: String = "PRI1") = """{ "data": ${caseJson(tierScore = null, crn = crn, prisonNumber = prisonNumber)}, 
  |"upstreamFailures": [${tierTimeoutFailure()}] }
""".trimMargin()
