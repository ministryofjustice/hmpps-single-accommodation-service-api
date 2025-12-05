package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.case.response

import java.time.LocalDate

fun expectedGetCasesResponse(
  currentAccommodationEndDate: LocalDate,
  nextAccommodationStartDate: LocalDate,
): String = """
[
  {
    "name": "First Middle Last",
    "dateOfBirth": "2000-12-03",
    "crn": "XX12345X",
    "prisonNumber": "PRI1",
    "tier": "Tier 1",
    "riskLevel": "VERY_HIGH",
    "pncReference": "Some PNC Reference",
    "assignedTo": {
      "id": 1,
      "name": "Team 1"
    },
    "currentAccommodation": {
      "type": "AIRBNB",
      "endDate": "$currentAccommodationEndDate"
    },
    "nextAccommodation": {
      "type": "PRISON",
      "startDate": "$nextAccommodationStartDate"
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
    "assignedTo": {
      "id": 1,
      "name": "Team 1"
    },
    "currentAccommodation": {
      "type": "AIRBNB",
      "endDate": "$currentAccommodationEndDate"
    },
    "nextAccommodation": {
      "type": "PRISON",
      "startDate": "$nextAccommodationStartDate"
    }
  }
]
""".trimIndent()

fun expectedGetCasesWithFilterResponse(
  currentAccommodationEndDate: LocalDate,
  nextAccommodationStartDate: LocalDate,
): String = """
[  
  {
    "name": "Zack Middle Smith",
    "dateOfBirth": "2000-12-03",
    "crn": "XY12345Z",
    "prisonNumber": "PRI1",
    "tier": "Tier 1",
    "riskLevel": "MEDIUM",
    "pncReference": "Some PNC Reference",
    "assignedTo": {
      "id": 1,
      "name": "Team 1"
    },
    "currentAccommodation": {
      "type": "AIRBNB",
      "endDate": "$currentAccommodationEndDate"
    },
    "nextAccommodation": {
      "type": "PRISON",
      "startDate": "$nextAccommodationStartDate"
    }
  }
]
""".trimIndent()
