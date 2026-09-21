package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.referralhistory.response

import java.util.UUID

fun expectedGetReferralHistory(
  id1: UUID,
  id2: UUID,
  id4: UUID,
  referralRejectionReason: String? = null,
  referralRejectionReasonDetail: String? = null,
  localAuthorityArea: String? = null,
  pdu: String? = null,
  placementAddress: String? = null,
  dtrId: UUID,
  dtrStatus: String? = null,
  dtrSubmissionDate: String? = null,
  withdrawalReason: String? = null,
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
        "applicationLastUpdatedDate": null,
        "referralRejectionReason": null,
        "referralRejectionReasonDetail": null,
        "localAuthorityArea": "Aberdeen City",
        "pdu": "Aberdeen City",
        "referredBy": {"name":"Test Data Setup User","username":"TEST_DATA_SETUP_USER"},
        "placementAddress": null,
        "placementStatus": null,
        "uiUrl": null,
        "withdrawalReason": null
     },
     {
        "id":"$id1",
        "type":"CAS1",
        "status":"NOT_ARRIVED",
        "assessmentStatus": null,
        "requestForPlacementStatus": "awaiting_match",
        "date":"2025-03-01",
        "applicationLastUpdatedDate": null,
        "referralRejectionReason": $referralRejectionReason,
        "referralRejectionReasonDetail": $referralRejectionReasonDetail,
        "localAuthorityArea": $localAuthorityArea,
        "pdu": $pdu,
        "referredBy": {"name":"Joe Bloggs","username":"user1"},
        "placementAddress": $placementAddress,
        "placementStatus": "notArrived",
        "uiUrl": "https://example.com/referral",
        "withdrawalReason": ${if (withdrawalReason != null) "\"$withdrawalReason\"" else "null"}
     },
     {
        "id":"$id2",
        "type":"CAS2",
        "status":"CANCELLED",
        "assessmentStatus": null,
        "requestForPlacementStatus": null,
        "date":"2025-02-15",
        "applicationLastUpdatedDate": "2025-02-20",
        "referralRejectionReason": $referralRejectionReason,
        "referralRejectionReasonDetail": null,
        "localAuthorityArea": $localAuthorityArea,
        "pdu": $pdu,
        "referredBy": {"name":"Joe Bloggs","username":null},
        "placementAddress": $placementAddress,
        "placementStatus": null,
        "uiUrl": "https://example.com/referral",
        "withdrawalReason": null
     },
     {
        "id":"$id4",
        "type":"CAS3",
        "status":"DEPARTED",
        "assessmentStatus": "ready_to_place",
        "requestForPlacementStatus": null,
        "date":"2025-02-01",
        "applicationLastUpdatedDate": null,
        "referralRejectionReason": $referralRejectionReason,
        "referralRejectionReasonDetail": $referralRejectionReasonDetail,
        "localAuthorityArea": $localAuthorityArea,
        "pdu": $pdu,
        "referredBy": {"name":"Joe Bloggs","username":"user1"},
        "placementAddress": $placementAddress,
        "placementStatus": "departed",
        "uiUrl": "https://example.com/referral",
        "withdrawalReason": null
     }
    ]
  }
  """.trimIndent()
