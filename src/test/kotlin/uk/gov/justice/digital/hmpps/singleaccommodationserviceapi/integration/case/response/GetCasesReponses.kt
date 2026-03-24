package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.case.response

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.TestData

@TestData
fun expectedGetCasesResponse(): String = """
[ {
  "name" : "First Middle Last",
  "dateOfBirth" : "2000-12-03",
  "crn" : "FAKECRN1",
  "prisonNumber" : "PRI1",
  "photoUrl" : null,
  "tierScore" : "C1",
  "tier" : "C1",
  "riskLevel" : "VERY_HIGH",
  "pncReference" : "Some PNC Reference",
  "assignedTo" : {
    "id" : 1,
    "name" : "Team 1",
    "username" : "Team 1"
  },
  "currentAccommodation" : null,
  "nextAccommodation" : null,
  "status":"RISK_OF_NO_FIXED_ABODE",
  "actions":[]
}, {
  "name" : "Zack Middle Smith",
  "dateOfBirth" : "2000-12-03",
  "crn" : "FAKECRN2",
  "prisonNumber" : "PRI1",
  "photoUrl" : null,
    "tierScore" : "C1",
  "tier" : "C1",
  "riskLevel" : "MEDIUM",
  "pncReference" : "Some PNC Reference",
  "assignedTo" : {
    "id" : 1,
    "name" : "Team 1",
    "username" : "Team 1"
  },
  "currentAccommodation" : null,
  "nextAccommodation" : null,
  "status":"RISK_OF_NO_FIXED_ABODE",
  "actions":[]
} ]
""".trimIndent()

fun expectedGetCasesWithFilterResponse(): String = """
 [ {
  "name" : "Zack Middle Smith",
  "dateOfBirth" : "2000-12-03",
  "crn" : "FAKECRN2",
  "prisonNumber" : "PRI1",
  "photoUrl" : null,
    "tierScore" : "C1",
  "tier" : "C1",
  "riskLevel" : "MEDIUM",
  "pncReference" : "Some PNC Reference",
  "assignedTo" : {
    "id" : 1,
    "name" : "Team 1",
    "username" : "Team 1"
  },
  "currentAccommodation" : null,
  "nextAccommodation" : null,
  "status":"RISK_OF_NO_FIXED_ABODE",
  "actions":[]
} ]
""".trimIndent()
