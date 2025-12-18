package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.eligibility.response

fun expectedGetEligibilityResponse(crn: String): String = """
  {
    "crn": "$crn",
    "caseStatus": "ACTION_NEEDED",
    "caseActions": ["Action 1!!"],
    "cas1": {
      "serviceStatus": "NOT_ELIGIBLE",
      "suitableApplication": null,
      "actions": []
    },
    "cas2Hdc": {
      "serviceStatus": "NOT_ELIGIBLE",
      "suitableApplication": null,
      "actions": []
    },
    "cas2PrisonBail": {
      "serviceStatus": "NOT_STARTED",
      "suitableApplication": null,
      "actions": ["Action 1!!"]
    },
    "cas2CourtBail": {
      "serviceStatus": "UPCOMING",
      "suitableApplication": null,
      "actions": ["Action 1!!"]
    },
    "cas3": {
      "serviceStatus": "NOT_ELIGIBLE",
      "suitableApplication": null,
      "actions": []
    }
  }
""".trimIndent()
