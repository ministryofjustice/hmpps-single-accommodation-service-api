package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.otheraccommodationreferral.json

import java.util.UUID

fun createOtherAccommodationReferralRequestBody(
  submissionDate: String = "2026-02-20",
  referenceNumber: String? = "REF-001",
  status: String = "SUBMITTED",
  organisationName: String? = "Organisation name",
  website: String? = "https://www.charity.org",
  submissionNote: String? = null,
  email: String? = null,
  phoneNumber: String? = null,
  outcomeReason: String? = null,
  outcomeNote: String? = null,
): String = """
{
  "submissionDate": "$submissionDate",
  "status": "$status"${if (referenceNumber != null) {
  """,
  "referenceNumber": "$referenceNumber""""
} else {
  ""
}}${if (organisationName != null) {
  """,
  "organisationName": "$organisationName""""
} else {
  ""
}}${if (website != null) {
  """,
  "website": "$website""""
} else {
  ""
}}${if (submissionNote != null) {
  """,
  "submissionNote": "$submissionNote""""
} else {
  ""
}}${if (email != null) {
  """,
  "email": "$email""""
} else {
  ""
}}${if (phoneNumber != null) {
  """,
  "phoneNumber": "$phoneNumber""""
} else {
  ""
}}${if (outcomeReason != null) {
  """,
  "outcomeReason": "$outcomeReason""""
} else {
  ""
}}${if (outcomeNote != null) {
  """,
  "outcomeNote": "$outcomeNote""""
} else {
  ""
}}
}
""".trimIndent()

@Suppress("LongParameterList")
fun expectedOtherAccommodationReferralResponseBody(
  id: UUID,
  caseId: UUID,
  crn: String,
  submissionDate: String = "2026-02-20",
  referenceNumber: String? = "REF-001",
  status: String = "SUBMITTED",
  createdBy: String,
  createdByUsername: String,
  createdAt: String,
  organisationName: String? = "Organisation name",
  website: String? = "https://www.charity.org",
  submissionNote: String? = null,
  email: String? = null,
  phoneNumber: String? = null,
  outcomeReason: String? = null,
  outcomeNote: String? = null,
): String = """
{
  "caseId": "$caseId",
  "crn": "$crn",
  "status": "$status",
  "submission": {
    "id": "$id",
    "referenceNumber": ${if (referenceNumber != null) "\"$referenceNumber\"" else "null"},
    "submissionDate": "$submissionDate",
    "createdBy": "$createdBy",
    "createdByUsername": "$createdByUsername",
    "createdAt": "$createdAt",
    "organisationName": ${if (organisationName != null) "\"$organisationName\"" else "null"},
    "website": ${if (website != null) "\"$website\"" else "null"},
    "submissionNote": ${if (submissionNote != null) "\"$submissionNote\"" else "null"},
    "email": ${if (email != null) "\"$email\"" else "null"},
    "phoneNumber": ${if (phoneNumber != null) "\"$phoneNumber\"" else "null"},
    "outcomeReason": ${if (outcomeReason != null) "\"$outcomeReason\"" else "null"},
    "outcomeNote": ${if (outcomeNote != null) "\"$outcomeNote\"" else "null"}
  }
}
""".trimIndent()

fun otherAccommodationReferralNoteRequestBody(note: String): String = """
  {
    "note" : "$note"
  }
""".trimIndent()

@Suppress("LongParameterList")
fun expectedGetOtherAccommodationReferralResponseBody(
  id: UUID,
  caseId: UUID,
  crn: String,
  submissionDate: String = "2026-02-20",
  referenceNumber: String? = "REF-001",
  status: String = "SUBMITTED",
  createdBy: String,
  createdByUsername: String,
  createdAt: String,
  organisationName: String? = "Organisation name",
  website: String? = "https://www.charity.org",
  submissionNote: String? = null,
  email: String? = null,
  phoneNumber: String? = null,
): String = """{"data": ${expectedOtherAccommodationReferralResponseBody(
  id = id,
  caseId = caseId,
  crn = crn,
  submissionDate = submissionDate,
  referenceNumber = referenceNumber,
  status = status,
  createdBy = createdBy,
  createdByUsername = createdByUsername,
  createdAt = createdAt,
  organisationName = organisationName,
  website = website,
  submissionNote = submissionNote,
  email = email,
  phoneNumber = phoneNumber,
)}}"""

fun expectedSearchOtherAccommodationReferralResponseBody(referrals: List<String>): String = """{"data": [${referrals.joinToString(",")}]}"""
