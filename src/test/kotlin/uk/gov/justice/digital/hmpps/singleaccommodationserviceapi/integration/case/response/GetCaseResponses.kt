package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.case.response

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.TestData

@TestData
fun expectedGetCaseResponse(): String = """
{
  "data": {
    "name": "First Middle Last",
    "dateOfBirth": "2000-12-03",
    "crn": "FAKECRN1",
    "prisonNumber": "PRI1",
    "photoUrl": null,
    "tierScore": "A1",
    "riskLevel": "VERY_HIGH",
    "pncReference": "Some PNC Reference",
    "assignedTo": {
      "forename": "First",
      "surname": "Last",
      "username": "user1",
      "staffCode": "ABCD1234"
    },
    "currentAccommodation": null,
    "nextAccommodation": null,
    "status": null,
    "actions": [],
    "caseAccess": "FULL"
  }
}
""".trimIndent()

fun expectedGetCaseUnknownResponse(): String = """
{
  "data": {
    "name": null,
    "dateOfBirth": null,
    "crn": "FAKECRN1",
    "prisonNumber": null,
    "photoUrl": null,
    "tierScore": null,
    "riskLevel": null,
    "pncReference": null,
    "assignedTo": null,
    "currentAccommodation": null,
    "nextAccommodation": null,
    "status": null,
    "actions": [],
    "caseAccess": "UNKNOWN"
  },
  "upstreamFailures": [
    {
      "endpoint": "getCaseByCrn",
      "failureType": "UPSTREAM_HTTP_ERROR",
      "httpResponseStatus": "500 INTERNAL_SERVER_ERROR",
      "message": "500 Internal Server Error: [no body]",
      "identifier": null
    }
  ]
}
""".trimIndent()
