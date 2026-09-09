package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.otheraccommodationreferral.json

import java.util.UUID

fun createOorRequestBody(
  submissionDate: String = "2026-01-15",
  referenceNumber: String? = "OOR-REF-001",
  status: String = "SUBMITTED",
  outcomeReason: String? = null,
  submissionNote: String? = null,
  outcomeNote: String? = null,
): String = """
{
  "submissionDate": "$submissionDate",
  "status": "$status"${if (referenceNumber != null) {
  """,
  "referenceNumber": "$referenceNumber""""
} else {
  ""
}}${if (outcomeReason != null) {
  """,
  "outcomeReason": "$outcomeReason""""
} else {
  ""
}}${if (submissionNote != null) {
  """,
  "submissionNote": "$submissionNote""""
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

fun expectedOorResponseBody(
  id: UUID,
  caseId: UUID,
  crn: String,
  submissionDate: String = "2026-01-15",
  referenceNumber: String? = "OOR-REF-001",
  status: String = "SUBMITTED",
  createdBy: String,
  createdAt: String,
  outcomeReason: String? = null,
  submissionNote: String? = null,
  outcomeNote: String? = null,
  active: Boolean? = null,
): String = """
{
  "caseId": "$caseId",
  "crn": "$crn",
  "status": "$status",${if (active != null) "\n  \"active\": $active," else ""}
  "submission": {
    "id": "$id",
    "referenceNumber": ${if (referenceNumber != null) "\"$referenceNumber\"" else "null"},
    "submissionDate": "$submissionDate",
    "createdBy": "$createdBy",
    "createdAt": "$createdAt",
    "outcomeReason": ${if (outcomeReason != null) "\"$outcomeReason\"" else "null"},
    "submissionNote": ${if (submissionNote != null) "\"$submissionNote\"" else "null"},
    "outcomeNote": ${if (outcomeNote != null) "\"$outcomeNote\"" else "null"}
  }
}
""".trimIndent()

fun expectedGetOorResponseBody(
  id: UUID,
  caseId: UUID,
  crn: String,
  submissionDate: String = "2026-01-15",
  referenceNumber: String? = "OOR-REF-001",
  status: String = "SUBMITTED",
  createdBy: String,
  createdAt: String,
  outcomeReason: String? = null,
  submissionNote: String? = null,
  outcomeNote: String? = null,
  active: Boolean? = null,
): String = """{"data": ${expectedOorResponseBody(id, caseId, crn, submissionDate, referenceNumber, status, createdBy, createdAt, outcomeReason, submissionNote, outcomeNote, active)}}"""

fun oorNoteRequestBody(note: String): String = """
  {
    "note" : "$note"
  }
""".trimIndent()
