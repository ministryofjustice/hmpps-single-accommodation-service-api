package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.case.response

val expectedCaseDtoResponseMultipleJson = """
[
  {
    "name": "First Middle Last",
    "dateOfBirth": "2000-12-03",
    "crn": "XX12345X",
    "prisonNumber": "PRI1",
    "tier": "TODO()",
    "riskLevel": "VERY_HIGH",
    "pncReference": "Some PNC Reference",
    "assignedTo": {
      "id": 1,
      "name": "TODO(name)"
    },
    "currentAccommodation": {
      "type": "AIRBNB",
      "endDate": "2025-12-13"
    },
    "nextAccommodation": {
      "type": "PRISON",
      "startDate": "2026-03-13"
    }
  },
  {
    "name": "Zack Middle Smith",
    "dateOfBirth": "2000-12-03",
    "crn": "XY12345Z",
    "prisonNumber": "PRI1",
    "tier": "TODO()",
    "riskLevel": "VERY_HIGH",
    "pncReference": "Some PNC Reference",
    "assignedTo": {
      "id": 1,
      "name": "TODO(name)"
    },
    "currentAccommodation": {
      "type": "AIRBNB",
      "endDate": "2025-12-13"
    },
    "nextAccommodation": {
      "type": "PRISON",
      "startDate": "2026-03-13"
    }
  }
]
""".trimIndent()
