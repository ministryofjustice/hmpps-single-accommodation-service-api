package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.case.response

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mock.mockPhotoUrl

fun expectedGetCasesResponse(): String = """
[
  {
    "name": "First Middle Last",
    "dateOfBirth": "2000-12-03",
    "crn": "XX12345X",
    "prisonNumber": "PRI1",
    "tier": "Tier 1",
    "riskLevel": "VERY_HIGH",
    "pncReference": "Some PNC Reference",
    "photoUrl":"$mockPhotoUrl",
    "assignedTo": {
      "id": 1,
      "name": "Team 1"
    },
    "currentAccommodation": {
      "type": "CAS1_MOCK",
      "endDate": "1970-01-01"
    },
    "nextAccommodation": {
      "type": "PRIVATE_ADDRESS_MOCK",
      "startDate": "1970-01-01"
    }
  },
  {
    "name": "Zack Middle Smith",
    "dateOfBirth": "2000-12-03",
    "crn": "XY12345Z",
    "prisonNumber": "PRI1",
    "tier": "Tier 1",
    "riskLevel": "MEDIUM",
    "pncReference": "Some PNC Reference",
    "photoUrl":"$mockPhotoUrl",
    "assignedTo": {
      "id": 1,
      "name": "Team 1"
    },
    "currentAccommodation": {
      "type": "CAS1_MOCK",
      "endDate": "1970-01-01"
    },
    "nextAccommodation": {
      "type": "PRIVATE_ADDRESS_MOCK",
      "startDate": "1970-01-01"
    }
  }
]
""".trimIndent()

fun expectedGetCasesWithFilterResponse(): String = """
[  
  {
    "name": "Zack Middle Smith",
    "dateOfBirth": "2000-12-03",
    "crn": "XY12345Z",
    "prisonNumber": "PRI1",
    "tier": "Tier 1",
    "riskLevel": "MEDIUM",
    "pncReference": "Some PNC Reference",
    "photoUrl":"$mockPhotoUrl",
    "assignedTo": {
      "id": 1,
      "name": "Team 1"
    },
    "currentAccommodation": {
      "type": "CAS1_MOCK",
      "endDate": "1970-01-01"
    },
    "nextAccommodation": {
      "type": "PRIVATE_ADDRESS_MOCK",
      "startDate": "1970-01-01"
    }
  }
]
""".trimIndent()
