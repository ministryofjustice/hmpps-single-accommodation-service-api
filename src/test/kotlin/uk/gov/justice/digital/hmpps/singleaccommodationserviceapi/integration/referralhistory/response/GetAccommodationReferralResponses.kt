package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.referralhistory.response

import java.util.UUID

fun expectedGetReferralHistory(
  id1: UUID,
  id4: UUID,
  referralRejectionReason: String? = null,
  referralRejectionReasonDetail: String? = null,
  localAuthorityArea: String? = null,
  pdu: String? = null,
  placementAddress: String? = null,
  dtrId: UUID,
  dtrStatus: String? = null,
  dtrSubmissionDate: String? = null,
): String =
  """
  {
    "data": [
     {
        "id":"$dtrId",
        "type":"DTR",
        "status":"${dtrStatus ?: "REJECTED"}",
        "assessmentStatus": null,
        "requestForPlacementStatus": null,
        "date":"$dtrSubmissionDate",
        "referralRejectionReason": null,
        "referralRejectionReasonDetail": null,
        "localAuthorityArea": "Aberdeen City",
        "pdu": "Aberdeen City",
        "referredBy": {"name":"Test Data Setup User","username":"TEST_DATA_SETUP_USER"},
        "placementAddress": null,
        "placementStatus": null,
        "uiUrl": null
     },
     {
        "id":"$id1",
        "type":"CAS1",
        "status":"NOT_ARRIVED",
        "assessmentStatus": null,
        "requestForPlacementStatus": "awaiting_match",
        "date":"2025-03-01",
        "referralRejectionReason": $referralRejectionReason,
        "referralRejectionReasonDetail": $referralRejectionReasonDetail,
        "localAuthorityArea": $localAuthorityArea,
        "pdu": $pdu,
        "referredBy": {"name":"Joe Bloggs","username":"user1"},
        "placementAddress": $placementAddress,
        "placementStatus": "notArrived",
        "uiUrl": "https://example.com/referral"
     },
     {
        "id":"$id4",
        "type":"CAS3",
        "status":"DEPARTED",
        "assessmentStatus": "ready_to_place",
        "requestForPlacementStatus": null,
        "date":"2025-02-01",
        "referralRejectionReason": $referralRejectionReason,
        "referralRejectionReasonDetail": $referralRejectionReasonDetail,
        "localAuthorityArea": $localAuthorityArea,
        "pdu": $pdu,
        "referredBy": {"name":"Joe Bloggs","username":"user1"},
        "placementAddress": $placementAddress,
        "placementStatus": "departed",
        "uiUrl": "https://example.com/referral"
     }
    ]
  }
  """.trimIndent()
