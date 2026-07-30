package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.dutytorefer.json

import java.util.UUID

fun createDtrRequestBody(
  localAuthorityAreaId: UUID,
  submissionDate: String = "2026-01-15",
  referenceNumber: String? = "DTR-REF-001",
  status: String = "SUBMITTED",
  withdrawalReason: String? = null,
  withdrawalReasonOther: String? = null,
  outcomeReason: String? = null,
  submissionNote: String? = null,
  outcomeNote: String? = null,
): String = """
{
  "localAuthorityAreaId": "$localAuthorityAreaId",
  "submissionDate": "$submissionDate",
  "status": "$status"${if (referenceNumber != null) {
  """,
  "referenceNumber": "$referenceNumber""""
} else {
  ""
}}${if (withdrawalReason != null) {
  """,
  "withdrawalReason": "$withdrawalReason""""
} else {
  ""
}}${if (withdrawalReasonOther != null) {
  """,
  "withdrawalReasonOther": "$withdrawalReasonOther""""
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

fun expectedDtrResponseBody(
  id: UUID,
  caseId: UUID,
  crn: String,
  localAuthorityAreaId: UUID,
  localAuthorityAreaName: String? = null,
  submissionDate: String = "2026-01-15",
  referenceNumber: String? = "DTR-REF-001",
  status: String = "SUBMITTED",
  createdBy: String,
  createdAt: String,
  withdrawalReason: String? = null,
  withdrawalReasonOther: String? = null,
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
    "localAuthority": {
      "localAuthorityAreaId": "$localAuthorityAreaId",
      "localAuthorityAreaName": ${if (localAuthorityAreaName != null) "\"$localAuthorityAreaName\"" else "null"}
    },
    "referenceNumber": ${if (referenceNumber != null) "\"$referenceNumber\"" else "null"},
    "submissionDate": "$submissionDate",
    "createdBy": "$createdBy",
    "createdAt": "$createdAt",
    "withdrawalReason": ${if (withdrawalReason != null) "\"$withdrawalReason\"" else "null"},
    "withdrawalReasonOther": ${if (withdrawalReasonOther != null) "\"$withdrawalReasonOther\"" else "null"},
    "outcomeReason": ${if (outcomeReason != null) "\"$outcomeReason\"" else "null"},
    "submissionNote": ${if (submissionNote != null) "\"$submissionNote\"" else "null"},
    "outcomeNote": ${if (outcomeNote != null) "\"$outcomeNote\"" else "null"}
  }
}
""".trimIndent()

fun expectedGetDtrResponseBody(
  id: UUID,
  caseId: UUID,
  crn: String,
  localAuthorityAreaId: UUID,
  localAuthorityAreaName: String? = null,
  submissionDate: String = "2026-01-15",
  referenceNumber: String? = "DTR-REF-001",
  status: String = "SUBMITTED",
  createdBy: String,
  createdAt: String,
  withdrawalReason: String? = null,
  withdrawalReasonOther: String? = null,
  outcomeReason: String? = null,
  submissionNote: String? = null,
  outcomeNote: String? = null,
  active: Boolean? = null,
): String = """{"data": ${expectedDtrResponseBody(id, caseId, crn, localAuthorityAreaId, localAuthorityAreaName, submissionDate, referenceNumber, status, createdBy, createdAt, withdrawalReason, withdrawalReasonOther, outcomeReason, submissionNote, outcomeNote, active)}}"""

fun dtrNoteRequestBody(note: String): String = """
  {
    "note" : "$note"
  }
""".trimIndent()

fun expectedDutyToReferUpdatedDomainEventJson(dutyToReferId: UUID) = """
{
  "aggregateId": "$dutyToReferId",
  "type": "SAS_DUTY_TO_REFER_UPDATED"
}
""".trimIndent()
