package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.eligibility.response

import java.util.UUID

fun expectedGetEligibilityResponse(crn: String, cas1SuitableApplicationId: UUID): String = """
{
  "crn": "$crn",
  "cas1": {
    "serviceStatus": "SUBMITTED",
    "suitableApplicationId": "$cas1SuitableApplicationId",
    "action": {
      "text":"Wait for approved premised (CAS1) assessment result",
      "isUpcoming":false
    },
    "link":"View application"
  },
  "cas2Hdc": {
    "serviceStatus": "NOT_ELIGIBLE",
    "suitableApplicationId": null,
    "action": null,
    "link":null
  },
  "cas2PrisonBail": {
    "serviceStatus": "NOT_ELIGIBLE",
    "suitableApplicationId": null,
    "action": null,
    "link":null
  },
  "cas2CourtBail": {
    "serviceStatus": "NOT_ELIGIBLE",
    "suitableApplicationId": null,
    "action": null,
    "link":null
  },
  "cas3": {
    "serviceStatus": "NOT_ELIGIBLE",
    "suitableApplicationId": null,
    "action": null,
    "link":null
  },
  "caseActions": [
    "Wait for approved premised (CAS1) assessment result"
  ],
  "caseStatus": "ACTION_NEEDED"
}
""".trimIndent()
