package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.case.response

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.TestData

@TestData
fun expectedGetCaseResponse(): String = """
{
  "data": {
    "forename": "First",
    "middleNames": "Middle",
    "surname": "Last",
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
      "username": "user1"
    },
    "userAccess": "FULL",
    "limitedAccess": false,
    "accommodationSummaries": null
  }
}
""".trimIndent()
fun expectedGetCaseResponseSearch(): String = """
{
  "data": {
    "forename": "First",
    "middleNames": "Middle",
    "surname": "Last",
    "dateOfBirth": "2000-12-03",
    "crn": "FAKECRN1",
    "prisonNumber": "PRI1",
    "photoUrl": null,
    "tierScore": null,
    "riskLevel": "VERY_HIGH",
    "pncReference": "Some PNC Reference",
    "assignedTo": {
      "forename": "First",
      "surname": "Last",
      "username": "user1"
    },
    "userAccess": "FULL",
    "limitedAccess": false,
    "accommodationSummaries": null
  }
}
""".trimIndent()
