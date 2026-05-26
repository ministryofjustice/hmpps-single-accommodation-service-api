package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.referralhistory.response

import java.util.UUID

fun expectedGetReferralHistory(
  id1: UUID,
  id2: UUID,
  id3: UUID,
  id4: UUID,
  referralRejectionReason: String? = null,
  localAuthorityArea: String? = null,
  pdu: String? = null,
  referredBy: String? = null,
  placementAddress: String? = null,
  placementStatus: String? = null,
): String = """
{
  "data": [
   {
      "id":"$id3",
      "type":"CAS2v2",
      "status":"ACCEPTED",
      "date":"2025-04-01T00:00:00Z",
      "referralRejectionReason": ${referralRejectionReason?.let { "\"$it\"" } ?: "null"},
      "localAuthorityArea": ${localAuthorityArea?.let { "\"$it\"" } ?: "null"},
      "pdu": ${pdu?.let { "\"$it\"" } ?: "null"},
      "referredBy": ${referredBy?.let { "\"$it\"" } ?: "null"},
      "placementAddress": ${placementAddress?.let { "\"$it\"" } ?: "null"},
      "placementStatus": ${placementStatus?.let { "\"$it\"" } ?: "null"}
   },
   {
      "id":"$id1",
      "type":"CAS1",
      "status":"PENDING",
      "date":"2025-03-01T00:00:00Z",
      "referralRejectionReason": ${referralRejectionReason?.let { "\"$it\"" } ?: "null"},
      "localAuthorityArea": ${localAuthorityArea?.let { "\"$it\"" } ?: "null"},
      "pdu": ${pdu?.let { "\"$it\"" } ?: "null"},
      "referredBy": ${referredBy?.let { "\"$it\"" } ?: "null"},
      "placementAddress": ${placementAddress?.let { "\"$it\"" } ?: "null"},
      "placementStatus": ${placementStatus?.let { "\"$it\"" } ?: "null"}
   },
   {
      "id":"$id4",
      "type":"CAS3",
      "status":"PENDING",
      "date":"2025-02-01T00:00:00Z",
      "referralRejectionReason": ${referralRejectionReason?.let { "\"$it\"" } ?: "null"},
      "localAuthorityArea": ${localAuthorityArea?.let { "\"$it\"" } ?: "null"},
      "pdu": ${pdu?.let { "\"$it\"" } ?: "null"},
      "referredBy": ${referredBy?.let { "\"$it\"" } ?: "null"},
      "placementAddress": ${placementAddress?.let { "\"$it\"" } ?: "null"},
      "placementStatus": ${placementStatus?.let { "\"$it\"" } ?: "null"}
   },
   {
      "id":"$id2",
      "type":"CAS2",
      "status":"PENDING",
      "date":"2025-01-01T00:00:00Z",
      "referralRejectionReason": ${referralRejectionReason?.let { "\"$it\"" } ?: "null"},
      "localAuthorityArea": ${localAuthorityArea?.let { "\"$it\"" } ?: "null"},
      "pdu": ${pdu?.let { "\"$it\"" } ?: "null"},
      "referredBy": ${referredBy?.let { "\"$it\"" } ?: "null"},
      "placementAddress": ${placementAddress?.let { "\"$it\"" } ?: "null"},
      "placementStatus": ${placementStatus?.let { "\"$it\"" } ?: "null"}
   }
  ]
}
""".trimIndent()
