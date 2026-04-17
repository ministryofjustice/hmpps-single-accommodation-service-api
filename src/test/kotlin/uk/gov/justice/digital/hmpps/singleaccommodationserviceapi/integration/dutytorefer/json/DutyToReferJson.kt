package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.dutytorefer.json

import java.util.UUID

fun createDtrRequestBody(
  localAuthorityAreaId: UUID,
  submissionDate: String = "2026-01-15",
  referenceNumber: String? = "DTR-REF-001",
  status: String = "SUBMITTED",
): String = """
{
  "localAuthorityAreaId": "$localAuthorityAreaId",
  "submissionDate": "$submissionDate",
  "status": "$status"${if (referenceNumber != null) {
  """,
  "referenceNumber": "$referenceNumber""""
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
): String = """
{
  "data": {
  "caseId": "$caseId",
  "crn": "$crn",
  "status": "$status",
  "submission": {
    "id": "$id",
    "localAuthority": {
      "localAuthorityAreaId": "$localAuthorityAreaId",
      "localAuthorityAreaName": ${if (localAuthorityAreaName != null) "\"$localAuthorityAreaName\"" else "null"}
    },
    "referenceNumber": ${if (referenceNumber != null) "\"$referenceNumber\"" else "null"},
    "submissionDate": "$submissionDate",
    "createdBy": "$createdBy",
    "createdAt": "$createdAt"
  }
  },
  "upstreamFailures": []
}
""".trimIndent()

fun expectedNotStartedDtrResponseBody(caseId: UUID, crn: String): String = """
{
  "data": {
    "caseId": "$caseId",
    "crn": "$crn",
    "status": "NOT_STARTED",
    "submission": null
  },
  "upstreamFailures": []
}
""".trimIndent()

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
