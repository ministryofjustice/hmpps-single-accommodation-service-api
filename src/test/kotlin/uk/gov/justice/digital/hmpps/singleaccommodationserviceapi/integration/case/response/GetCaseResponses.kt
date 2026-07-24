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
      "username": "user1"
    },
    "actions": [],
    "userAccess": "FULL",
    "limitedAccess": false
  }
}
""".trimIndent()
