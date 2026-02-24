package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.eligibility.response

import java.util.UUID

fun expectedGetEligibilityResponse(crn: String, cas1SuitableApplicationId: UUID): String = """
{
  "crn": "$crn",
  "cas1": {
    "serviceStatus": "SUBMITTED",
    "suitableApplicationId": "$cas1SuitableApplicationId",
    "action": {
      "text": "Await Assessment",
    }
  },
  "cas2Hdc": {
    "serviceStatus": "NOT_ELIGIBLE",
    "suitableApplicationId": null,
    "action": null
  },
  "cas2PrisonBail": {
    "serviceStatus": "NOT_ELIGIBLE",
    "suitableApplicationId": null,
    "action": null
  },
  "cas2CourtBail": {
    "serviceStatus": "NOT_ELIGIBLE",
    "suitableApplicationId": null,
    "action": null
  },
  "cas3": {
    "serviceStatus": "NOT_ELIGIBLE",
    "suitableApplicationId": null,
    "action": null
  },
  "caseActions": [
    "Await Assessment"
  ],
}
""".trimIndent()
