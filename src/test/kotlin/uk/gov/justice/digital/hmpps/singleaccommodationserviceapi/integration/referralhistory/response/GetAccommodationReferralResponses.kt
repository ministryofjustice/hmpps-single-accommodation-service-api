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
  placementStatus: String? = null,
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
        "status":"${dtrStatus ?: "PENDING"}",
        "date":"${dtrSubmissionDate}T00:00:00Z",
        "referralRejectionReason": null,
        "referralRejectionReasonDetail": null,
        "localAuthorityArea": "Aberdeen City",
        "pdu": "Aberdeen City",
        "referredBy": {"name":"Test Data Setup User","username":"TEST_DATA_SETUP_USER","staffCode": null},
        "placementAddress": null,
        "placementStatus": null,
        "referralUrl": null
     },
     {
        "id":"$id1",
        "type":"CAS1",
        "status":"PENDING",
        "date":"2025-03-01T00:00:00Z",
        "referralRejectionReason": $referralRejectionReason,
        "referralRejectionReasonDetail": $referralRejectionReasonDetail,
        "localAuthorityArea": $localAuthorityArea,
        "pdu": $pdu,
        "referredBy": {"name":"Joe Bloggs","username":"user1","staffCode":"ABCD1234"},
        "placementAddress": $placementAddress,
        "placementStatus": $placementStatus,
        "referralUrl": "https://example.com/referral"
     },
     {
        "id":"$id4",
        "type":"CAS3",
        "status":"PENDING",
        "date":"2025-02-01T00:00:00Z",
        "referralRejectionReason": $referralRejectionReason,
        "referralRejectionReasonDetail": $referralRejectionReasonDetail,
        "localAuthorityArea": $localAuthorityArea,
        "pdu": $pdu,
        "referredBy": {"name":"Joe Bloggs","username":"user1","staffCode":"ABCD1234"},
        "placementAddress": $placementAddress,
        "placementStatus": $placementStatus,
        "referralUrl": "https://example.com/referral"
     }
    ]
  }
  """.trimIndent()
