package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.case.response

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mock.mockPhotoUrl
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.TestData

@TestData
fun expectedGetCasesResponse(): String = """
[
  {
    "name": "First Middle Last",
    "dateOfBirth": "2000-12-03",
    "crn": "XX12345X",
    "prisonNumber": "PRI1",
    "photoUrl": "$mockPhotoUrl",
    "tier": "C1",
    "riskLevel": "VERY_HIGH",
    "pncReference": "Some PNC Reference",
    "assignedTo": {
      "id": 1,
      "name": "Team 1"
    },
    "currentAccommodation": {
      "type": "PRISON",
      "subType": "RENTED",
      "name": "!!TODO()",
      "isSettled": true,
      "offenderReleaseType": "BAIL",
      "startDate": "2025-12-17",
      "endDate": "2025-12-27",
      "address": {
        "line1": "!!val line1: String,",
        "line2": "!!val line2: String?,",
        "region": "!!val region: String?,",
        "city": "!!val city: String,",
        "postCode": "!!val postCode: String,"
      }
    },
    "nextAccommodation": {
      "type": "NO_FIXED_ABODE",
      "subType": null,
      "name": null,
      "isSettled": null,
      "offenderReleaseType": null,
      "startDate": null,
      "endDate": null,
      "address": null
    }
  },
  {
    "name": "Zack Middle Smith",
    "dateOfBirth": "2000-12-03",
    "crn": "XY12345Z",
    "prisonNumber": "PRI1",
    "photoUrl": "$mockPhotoUrl",
    "tier": "C1",
    "riskLevel": "MEDIUM",
    "pncReference": "Some PNC Reference",
    "assignedTo": {
      "id": 1,
      "name": "Team 1"
    },
    "currentAccommodation": {
      "type": "PRISON",
      "subType": "RENTED",
      "name": "!!TODO()",
      "isSettled": true,
      "offenderReleaseType": "BAIL",
      "startDate": "2025-12-17",
      "endDate": "2025-12-27",
      "address": {
        "line1": "!!val line1: String,",
        "line2": "!!val line2: String?,",
        "region": "!!val region: String?,",
        "city": "!!val city: String,",
        "postCode": "!!val postCode: String,"
      }
    },
    "nextAccommodation": {
      "type": "NO_FIXED_ABODE",
      "subType": null,
      "name": null,
      "isSettled": null,
      "offenderReleaseType": null,
      "startDate": null,
      "endDate": null,
      "address": null
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
    "photoUrl": "$mockPhotoUrl",
    "tier": "C1",
    "riskLevel": "MEDIUM",
    "pncReference": "Some PNC Reference",
    "assignedTo": {
      "id": 1,
      "name": "Team 1"
    },
    "currentAccommodation": {
      "type": "PRISON",
      "subType": "RENTED",
      "name": "!!TODO()",
      "isSettled": true,
      "offenderReleaseType": "BAIL",
      "startDate": "2025-12-17",
      "endDate": "2025-12-27",
      "address": {
        "line1": "!!val line1: String,",
        "line2": "!!val line2: String?,",
        "region": "!!val region: String?,",
        "city": "!!val city: String,",
        "postCode": "!!val postCode: String,"
      }
    },
    "nextAccommodation": {
      "type": "NO_FIXED_ABODE",
      "subType": null,
      "name": null,
      "isSettled": null,
      "offenderReleaseType": null,
      "startDate": null,
      "endDate": null,
      "address": null
    }
  }
]
""".trimIndent()
