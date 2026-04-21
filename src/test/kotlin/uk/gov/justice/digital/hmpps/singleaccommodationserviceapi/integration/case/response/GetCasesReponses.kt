package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.case.response

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.TestData

@TestData
fun expectedGetCasesResponse(): String = """
[{"name":"First Middle Last","dateOfBirth":"2000-12-03","crn":"FAKECRN1","prisonNumber":"PRI1","photoUrl":null,"tierScore":null,"tier":null,"riskLevel":null,"pncReference":"Some PNC Reference","assignedTo":{"id":1,"name":"Team 1","username":null,"staffCode":null},"currentAccommodation":null,"nextAccommodation":null,"status":null,"actions":[]},{"name":"Zack Middle Smith","dateOfBirth":"2000-12-03","crn":"FAKECRN2","prisonNumber":"PRI2","photoUrl":null,"tierScore":null,"tier":null,"riskLevel":null,"pncReference":"Some PNC Reference","assignedTo":{"id":1,"name":"Team 1","username":null,"staffCode":null},"currentAccommodation":null,"nextAccommodation":null,"status":null,"actions":[]}]
""".trimIndent()

@TestData
fun expectedGetCasesV2Response(): String = """
{"data":[{"name":"First Middle Last","dateOfBirth":"2000-12-03","crn":"FAKECRN1","prisonNumber":"PRI1","photoUrl":null,"tierScore":null,"tier":null,"riskLevel":null,"pncReference":"Some PNC Reference","assignedTo":{"id":1,"name":"Team 1","username":null,"staffCode":null},"currentAccommodation":null,"nextAccommodation":null,"status":null,"actions":[]},{"name":"Zack Middle Smith","dateOfBirth":"2000-12-03","crn":"FAKECRN2","prisonNumber":"PRI2","photoUrl":null,"tierScore":null,"tier":null,"riskLevel":null,"pncReference":"Some PNC Reference","assignedTo":{"id":1,"name":"Team 1","username":null,"staffCode":null},"currentAccommodation":null,"nextAccommodation":null,"status":null,"actions":[]}]}
""".trimIndent()

@TestData
fun expectedGetCaseV2Response(): String = """
{
  "data": {
    "name" : "First Middle Last",
    "dateOfBirth" : "2000-12-03",
    "crn" : "FAKECRN1",
    "prisonNumber" : "PRI1",
    "photoUrl" : null,
    "tierScore" : "A1",
    "tier" : "A1",
    "riskLevel" : "VERY_HIGH",
    "pncReference" : "Some PNC Reference",
    "assignedTo" : {
      "id" : 1,
      "name" : "Team 1",
      "username" : null,
      "staffCode" : null
    },
    "currentAccommodation" : null,
    "nextAccommodation" : null,
    "status":null,
    "actions":[]
  }
}
""".trimIndent()

fun expectedGetCasesWithFilterResponse(): String = """
 []
""".trimIndent()
