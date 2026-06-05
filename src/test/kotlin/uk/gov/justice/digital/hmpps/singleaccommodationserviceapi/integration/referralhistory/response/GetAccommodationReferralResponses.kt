package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.referralhistory.response

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.DeliusUserDto
import java.util.UUID

fun expectedGetReferralHistory(
  id1: UUID,
  id2: UUID,
  id3: UUID,
  id4: UUID,
  referralRejectionReason: String? = null,
  referralRejectionReasonDetail: String? = null,
  localAuthorityArea: String? = null,
  pdu: String? = null,
  referredBy: DeliusUserDto? = null,
  placementAddress: String? = null,
  placementStatus: String? = null,
): String {
  fun referredByJson(dto: DeliusUserDto?) = if (dto == null) {
    "null"
  } else {
    """{
          "name": "${dto.name}",
          "username": "${dto.username}",
          "staffCode": "${dto.staffCode}" 
       }
    """.trimIndent()
  }

  return """
  {
    "data": [
     {
        "id":"$id3",
        "type":"CAS2v2",
        "status":"ACCEPTED",
        "date":"2025-04-01T00:00:00Z",
        "referralRejectionReason": $referralRejectionReason,
        "referralRejectionReasonDetail": $referralRejectionReasonDetail,
        "localAuthorityArea": $localAuthorityArea,
        "pdu": $pdu,
        "referredBy": ${referredByJson(referredBy)},
        "placementAddress": $placementAddress,
        "placementStatus": $placementStatus
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
        "referredBy": ${referredByJson(referredBy)},
        "placementAddress": $placementAddress,
        "placementStatus": $placementStatus
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
        "referredBy": ${referredByJson(referredBy)},
        "placementAddress": $placementAddress,
        "placementStatus": $placementStatus
     },
     {
        "id":"$id2",
        "type":"CAS2",
        "status":"PENDING",
        "date":"2025-01-01T00:00:00Z",
        "referralRejectionReason": $referralRejectionReason,
        "referralRejectionReasonDetail": $referralRejectionReasonDetail,
        "localAuthorityArea": $localAuthorityArea,
        "pdu": $pdu,
        "referredBy": ${referredByJson(referredBy)},
        "placementAddress": $placementAddress,
        "placementStatus": $placementStatus
     }
    ]
  }
  """.trimIndent()
}
