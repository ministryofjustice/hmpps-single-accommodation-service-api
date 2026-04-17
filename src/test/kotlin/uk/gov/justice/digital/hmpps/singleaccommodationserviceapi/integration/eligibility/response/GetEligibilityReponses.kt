package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.eligibility.response

import java.util.UUID

fun expectedGetEligibilityResponse(crn: String, cas1SuitableApplicationId: UUID): String = """
{
  "data": {
  "crn": "$crn",
  "cas1": {
    "serviceStatus": "SUBMITTED",
    "suitableApplicationId": "$cas1SuitableApplicationId",
    "action": "Wait for approved premise (CAS1) assessment result",
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
    "Wait for approved premise (CAS1) assessment result"
  ]
  },
  "upstreamFailures": []
}
""".trimIndent()
