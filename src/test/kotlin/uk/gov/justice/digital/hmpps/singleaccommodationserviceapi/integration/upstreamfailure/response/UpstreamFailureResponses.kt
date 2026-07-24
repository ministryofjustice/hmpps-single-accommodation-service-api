package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.upstreamfailure.response

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.TestData

private fun caseJson(
  name: String? = "First Middle Last",
  dateOfBirth: String? = "2000-12-03",
  crn: String = "FAKECRN1",
  prisonNumber: String? = "PRI1",
  tierScore: String? = "A1",
  riskLevel: String? = "VERY_HIGH",
  pncReference: String? = "Some PNC Reference",
  assignedTo: String = """{"forename":"First","surname":"Last","username":"user1"}""",
) = """
{
  "name": ${if (name != null) "\"$name\"" else "null"},
  "dateOfBirth": ${if (dateOfBirth != null) "\"$dateOfBirth\"" else "null"},
  "crn": "$crn",
  "prisonNumber": ${if (prisonNumber != null) "\"$prisonNumber\"" else "null"},
  "photoUrl": null,
  "tierScore": ${if (tierScore != null) "\"$tierScore\"" else "null"},
  "riskLevel": ${if (riskLevel != null) "\"$riskLevel\"" else "null"},
  "pncReference": ${if (pncReference != null) "\"$pncReference\"" else "null"},
  "assignedTo": $assignedTo,
  "actions": [],
  "userAccess": "FULL",
  "limitedAccess": false
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

private fun cprServerErrorFailure(identifierCrn: String? = null) = failureJson(
  endpoint = "getCorePersonRecordByCrn",
  failureType = "UPSTREAM_HTTP_ERROR",
  httpResponseStatus = "500 INTERNAL_SERVER_ERROR",
  message = "500 Internal Server Error: [no body]",
  identifierCrn = identifierCrn,
)

private fun tierServerErrorFailure(identifierCrn: String? = null) = failureJson(
  endpoint = "getTierByCrn",
  failureType = "UPSTREAM_HTTP_ERROR",
  httpResponseStatus = "500 INTERNAL_SERVER_ERROR",
  message = "500 Internal Server Error: [no body]",
  identifierCrn = identifierCrn,
)

private fun tierTimeoutFailure(crn: String, identifierCrn: String? = null) = failureJson(
  endpoint = "getTierByCrn",
  failureType = "TIMEOUT",
  message = "I/O error on GET request for \\\"http://localhost:PORT/v2/crn/$crn/tier\\\": Request cancelled",
  identifierCrn = identifierCrn,
)

private fun tierNotFoundFailure(identifierCrn: String? = null) = failureJson(
  endpoint = "getTierByCrn",
  failureType = "UPSTREAM_HTTP_ERROR",
  httpResponseStatus = "404 NOT_FOUND",
  message = "404 Not Found: [no body]",
  identifierCrn = identifierCrn,
)

@TestData
fun expectedSingleCrnTierServerError() = """{ "data": ${caseJson(tierScore = null)}, "upstreamFailures": [${tierServerErrorFailure()}] }"""

@TestData
fun expectedSingleCrnTierTimeout() = """{ "data": ${caseJson(tierScore = null)}, "upstreamFailures": [${tierTimeoutFailure("FAKECRN1")}] }"""
