package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.upstreamfailure.response

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.TestData

private fun caseJson(
  name: String? = "First Middle Last",
  dateOfBirth: String? = "2000-12-03",
  crn: String = "FAKECRN1",
  prisonNumber: String? = "PRI1",
  tierScore: String? = "A1",
  tier: String? = "A1",
  riskLevel: String? = "VERY_HIGH",
  pncReference: String? = "Some PNC Reference",
  assignedTo: String = """{ "id": 1, "name": "Team 1", "username": null, "staffCode": null }""",
) = """
{
  "name": ${if (name != null) "\"$name\"" else "null"},
  "dateOfBirth": ${if (dateOfBirth != null) "\"$dateOfBirth\"" else "null"},
  "crn": "$crn",
  "prisonNumber": ${if (prisonNumber != null) "\"$prisonNumber\"" else "null"},
  "photoUrl": null,
  "tierScore": ${if (tierScore != null) "\"$tierScore\"" else "null"},
  "tier": ${if (tier != null) "\"$tier\"" else "null"},
  "riskLevel": ${if (riskLevel != null) "\"$riskLevel\"" else "null"},
  "pncReference": ${if (pncReference != null) "\"$pncReference\"" else "null"},
  "assignedTo": $assignedTo,
  "currentAccommodation": null,
  "nextAccommodation": null,
  "status": null,
  "actions": []
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

private fun cprTimeoutFailure(crn: String, identifierCrn: String? = null) = failureJson(
  endpoint = "getCorePersonRecordByCrn",
  failureType = "TIMEOUT",
  message = "I/O error on GET request for \\\"http://localhost:PORT/person/probation/$crn\\\": Request cancelled",
  identifierCrn = identifierCrn,
)

private fun roshServerErrorFailure(identifierCrn: String? = null) = failureJson(
  endpoint = "getRoshSummaryByCrn",
  failureType = "UPSTREAM_HTTP_ERROR",
  httpResponseStatus = "500 INTERNAL_SERVER_ERROR",
  message = "500 Internal Server Error: [no body]",
  identifierCrn = identifierCrn,
)

private fun roshTimeoutFailure(crn: String, identifierCrn: String? = null) = failureJson(
  endpoint = "getRoshSummaryByCrn",
  failureType = "TIMEOUT",
  message = "I/O error on GET request for \\\"http://localhost:PORT/rosh/$crn\\\": Request cancelled",
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
  message = "I/O error on GET request for \\\"http://localhost:PORT/crn/$crn/tier\\\": Request cancelled",
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
fun expectedSingleCrnCprServerError() = """{ "data": ${caseJson(name = null, dateOfBirth = null, prisonNumber = null, pncReference = null)}, "upstreamFailures": [${cprServerErrorFailure()}] }"""

@TestData
fun expectedSingleCrnCprTimeout() = """{ "data": ${caseJson(name = null, dateOfBirth = null, prisonNumber = null, pncReference = null)}, "upstreamFailures": [${cprTimeoutFailure("FAKECRN1")}] }"""

@TestData
fun expectedSingleCrnRoshServerError() = """{ "data": ${caseJson(riskLevel = null)}, "upstreamFailures": [${roshServerErrorFailure()}] }"""

@TestData
fun expectedSingleCrnRoshTimeout() = """{ "data": ${caseJson(riskLevel = null)}, "upstreamFailures": [${roshTimeoutFailure("FAKECRN1")}] }"""

@TestData
fun expectedSingleCrnTierServerError() = """{ "data": ${caseJson(tierScore = null, tier = null)}, "upstreamFailures": [${tierServerErrorFailure()}] }"""

@TestData
fun expectedSingleCrnTierTimeout() = """{ "data": ${caseJson(tierScore = null, tier = null)}, "upstreamFailures": [${tierTimeoutFailure("FAKECRN1")}] }"""

@TestData
fun expectedSingleCrnAllUpstreamFailures() = """{ "data": ${caseJson(name = null, dateOfBirth = null, prisonNumber = null, pncReference = null, tierScore = null, tier = null, riskLevel = null)}, "upstreamFailures": [${cprServerErrorFailure()}, ${roshServerErrorFailure()}, ${tierServerErrorFailure()}] }"""
