package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.eligibility.response

import java.util.UUID

fun expectedGetEligibilityResponse(crn: String, cas1SuitableApplicationId: UUID, dtrCaseId: UUID): String = """
{
  "crn": "$crn",
  "cas1": {
    "serviceStatus": "SUBMITTED",
    "suitableApplicationId": "$cas1SuitableApplicationId",
    "action": "Wait for approved premise (CAS1) assessment result",
    "link": "View application"
  },
  "cas2Hdc": {
    "serviceStatus": "NOT_ELIGIBLE",
    "suitableApplicationId": null,
    "action": null,
    "link": null
  },
  "cas2PrisonBail": {
    "serviceStatus": "NOT_ELIGIBLE",
    "suitableApplicationId": null,
    "action": null,
    "link": null
  },
  "cas2CourtBail": {
    "serviceStatus": "NOT_ELIGIBLE",
    "suitableApplicationId": null,
    "action": null,
    "link": null
  },
  "cas3": {
    "serviceStatus": "NOT_ELIGIBLE",
    "suitableApplicationId": null,
    "action": null,
    "link": null
  },
  "dtr": {
    "serviceStatus": "NOT_STARTED",
    "suitableApplicationId": null,
    "action": "Add DTR referral details",
    "link": "Add referral details"
  },
  "caseActions": [
    "Add DTR referral details",
    "Wait for approved premise (CAS1) assessment result"
  ],
  "dutyToReferData": {
    "caseId": "$dtrCaseId",
    "crn": "$crn",
    "status": "NOT_STARTED",
    "submission": null
  }
}
""".trimIndent()
