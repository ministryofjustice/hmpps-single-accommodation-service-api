package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.referralhistory.response

import java.util.UUID

@Suppress("LongParameterList")
fun expectedReferralHistoryItem(
  id: UUID,
  type: String,
  status: String?,
  date: String?,
  referredByName: String,
  referredByUsername: String?,
  assessmentStatus: String? = null,
  requestForPlacementStatus: String? = null,
  applicationLastUpdatedDate: String? = null,
  referralRejectionReason: String? = null,
  referralRejectionReasonDetail: String? = null,
  localAuthorityArea: String? = null,
  pdu: String? = null,
  placementAddress: String? = null,
  placementStatus: String? = null,
  uiUrl: String? = null,
  withdrawalReason: String? = null,
  withdrawalNote: String? = null,
): String = """
  {
    "id":"$id",
    "type":"$type",
    "status":${status.toJsonValue()},
    "assessmentStatus": ${assessmentStatus.toJsonValue()},
    "requestForPlacementStatus": ${requestForPlacementStatus.toJsonValue()},
    "date":${date.toJsonValue()},
    "applicationLastUpdatedDate": ${applicationLastUpdatedDate.toJsonValue()},
    "referralRejectionReason": ${referralRejectionReason.toJsonValue()},
    "referralRejectionReasonDetail": ${referralRejectionReasonDetail.toJsonValue()},
    "localAuthorityArea": ${localAuthorityArea.toJsonValue()},
    "pdu": ${pdu.toJsonValue()},
    "referredBy": {"name":"$referredByName","username":${referredByUsername.toJsonValue()}},
    "placementAddress": ${placementAddress.toJsonValue()},
    "placementStatus": ${placementStatus.toJsonValue()},
    "uiUrl": ${uiUrl.toJsonValue()},
    "withdrawalReason": ${withdrawalReason.toJsonValue()},
    "withdrawalNote": ${withdrawalNote.toJsonValue()}
  }
""".trimIndent()

fun expectedGetReferralHistoryResponseBody(referrals: List<String>): String = """{"data": [${referrals.joinToString(",")}]}"""

private fun String?.toJsonValue(): String = if (this != null) "\"$this\"" else "null"
