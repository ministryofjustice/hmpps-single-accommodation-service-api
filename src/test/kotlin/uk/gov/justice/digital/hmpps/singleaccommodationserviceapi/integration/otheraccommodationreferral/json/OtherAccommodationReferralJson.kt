package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.otheraccommodationreferral.json

import java.util.UUID

fun createOtherAccommodationReferralRequestBody(
  localAuthorityAreaId: UUID,
  submissionDate: String = "2026-02-20",
  referenceNumber: String? = "REF-001",
  status: String = "SUBMITTED",
  organisationName: String? = "Organisation name",
  website: String? = "https://www.charity.org",
  submissionNote: String? = "A submission note",
): String = """
{
  "localAuthorityAreaId": "$localAuthorityAreaId",
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
}}
}
""".trimIndent()

@Suppress("LongParameterList")
fun expectedOtherAccommodationReferralResponseBody(
  id: UUID,
  caseId: UUID,
  crn: String,
  localAuthorityAreaId: UUID,
  localAuthorityAreaName: String? = null,
  submissionDate: String = "2026-02-20",
  referenceNumber: String? = "REF-001",
  status: String = "SUBMITTED",
  createdBy: String,
  createdByUsername: String,
  createdAt: String,
  organisationName: String? = "Organisation name",
  website: String? = "https://www.charity.org",
  submissionNote: String? = "A submission note",
): String = """
{
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
    "createdByUsername": "$createdByUsername",
    "createdAt": "$createdAt",
    "organisationName": ${if (organisationName != null) "\"$organisationName\"" else "null"},
    "website": ${if (website != null) "\"$website\"" else "null"},
    "submissionNote": ${if (submissionNote != null) "\"$submissionNote\"" else "null"}
  }
}
""".trimIndent()
