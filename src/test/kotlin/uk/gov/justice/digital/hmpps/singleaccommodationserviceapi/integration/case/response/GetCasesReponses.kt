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
  "tier" : "C1",
  "riskLevel" : "VERY_HIGH",
  "pncReference" : "Some PNC Reference",
  "assignedTo" : {
    "id" : 1,
    "name" : "Team 1"
  },
  "currentAccommodation" : null,
  "nextAccommodation" : null
}, {
  "name" : "Zack Middle Smith",
  "dateOfBirth" : "2000-12-03",
  "crn" : "FAKECRN2",
  "prisonNumber" : "PRI1",
  "photoUrl" : null,
  "tier" : "C1",
  "riskLevel" : "MEDIUM",
  "pncReference" : "Some PNC Reference",
  "assignedTo" : {
    "id" : 1,
    "name" : "Team 1"
  },
  "currentAccommodation" : null,
  "nextAccommodation" : null
} ]
""".trimIndent()

fun expectedGetCasesWithFilterResponse(): String = """
 [ {
  "name" : "Zack Middle Smith",
  "dateOfBirth" : "2000-12-03",
  "crn" : "FAKECRN2",
  "prisonNumber" : "PRI1",
  "photoUrl" : null,
  "tier" : "C1",
  "riskLevel" : "MEDIUM",
  "pncReference" : "Some PNC Reference",
  "assignedTo" : {
    "id" : 1,
    "name" : "Team 1"
  },
  "currentAccommodation" : null,
  "nextAccommodation" : null
} ]
""".trimIndent()
