package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.dutytorefer.json

import java.util.UUID

fun createDtrRequestBody(
  localAuthorityAreaId: UUID,
  submissionDate: String = "2026-01-15",
  referenceNumber: String? = "DTR-REF-001",
): String = """
{
  "localAuthorityAreaId": "$localAuthorityAreaId",
  "submissionDate": "$submissionDate"${if (referenceNumber != null) {
  """,
  "referenceNumber": "$referenceNumber""""
} else {
  ""
}}
}
""".trimIndent()

fun expectedCreateDtrResponseBody(
  id: UUID,
  crn: String,
  localAuthorityAreaId: UUID,
  submissionDate: String = "2026-01-15",
  referenceNumber: String? = "DTR-REF-001",
  createdBy: String,
  createdAt: String,
): String = """
{
  "crn": "$crn",
  "serviceStatus": "SUBMITTED",
  "action": null,
  "submission": {
    "id": "$id",
    "localAuthorityAreaId": "$localAuthorityAreaId",
    "localAuthorityAreaName": null,
    "referenceNumber": ${if (referenceNumber != null) "\"$referenceNumber\"" else "null"},
    "submissionDate": "$submissionDate",
    "outcomeStatus": null,
    "outcomeDate": null,
    "createdBy": "$createdBy",
    "createdAt": "$createdAt"
  }
}
""".trimIndent()

fun expectedDutyToReferCreatedDomainEventJson(dutyToReferId: UUID) = """
{
  "aggregateId": "$dutyToReferId",
  "type": "SAS_DUTY_TO_REFER_CREATED"
}
""".trimIndent()
