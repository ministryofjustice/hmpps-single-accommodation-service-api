package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.externalreferral.json

import java.util.UUID

private const val SUBMISSION_NOTE = "A submission note"

@Suppress("LongParameterList")
fun expectedGetExternalReferralOutcomeTimelineResponse(
  externalReferralId: UUID,
  caseId: UUID,
  crn: String,
  createCommitTime: String,
  updateCommitTime: String,
  newStatus: String,
  outcomeNote: String,
): String = """
{
   "data":[
      {
         "type":"UPDATE",
         "author":"Delius User",
         "authorDetails":{
            "forename":"Delius",
            "surname":"User",
            "username":"DELIUS_USER"
         },
         "commitDate":"$updateCommitTime",
         "changes":[
            {
               "field":"status",
               "value":"$newStatus",
               "oldValue":"SUBMITTED"
            },
            {
               "field":"outcomeNote",
               "value":"$outcomeNote",
               "oldValue":null
            }
         ]
      },
      ${expectedCreateTimelineEntry(externalReferralId, caseId, crn, createCommitTime)}
   ]
}
""".trimIndent()

fun expectedGetExternalReferralTimelineResponse(
  externalReferralId: UUID,
  caseId: UUID,
  crn: String,
  createCommitTime: String,
): String = """
{
   "data":[
      ${expectedCreateTimelineEntry(externalReferralId, caseId, crn, createCommitTime, submissionNote = SUBMISSION_NOTE)}
   ]
}
""".trimIndent()

@Suppress("LongParameterList")
fun expectedGetExternalReferralTimelineResponse(
  externalReferralId: UUID,
  caseId: UUID,
  crn: String,
  referenceNumber: String,
  previousReferenceNumber: String,
  createCommitTime: String,
  createNoteCommitTime: String,
  updateCommitTime: String,
): String = """
{
   "data":[
      {
         "type":"UPDATE",
         "author":"Delius User",
         "authorDetails":{
            "forename":"Delius",
            "surname":"User",
            "username":"DELIUS_USER"
         },
         "commitDate":"$updateCommitTime",
         "changes":[
            {
               "field":"referenceNumber",
               "value":"$referenceNumber",
               "oldValue":"$previousReferenceNumber"
            }
         ]
      },
      {
         "type":"NOTE",
         "author":"Delius User",
         "authorDetails":{
            "forename":"Delius",
            "surname":"User",
            "username":"DELIUS_USER"
         },
         "commitDate":"$createNoteCommitTime",
         "changes":[
            {
               "field":"note",
               "value":"Test note",
               "oldValue":null
            }
         ]
      },
      ${expectedCreateTimelineEntry(externalReferralId, caseId, crn, createCommitTime, previousReferenceNumber, SUBMISSION_NOTE)}
   ]
}
""".trimIndent()

fun expectedGetExternalReferralWithdrawalTimelineResponse(
  externalReferralId: UUID,
  caseId: UUID,
  crn: String,
  createCommitTime: String,
  updateCommitTime: String,
): String = """
{
   "data":[
      {
         "type":"UPDATE",
         "author":"Delius User",
         "authorDetails":{
            "forename":"Delius",
            "surname":"User",
            "username":"DELIUS_USER"
         },
         "commitDate":"$updateCommitTime",
         "changes":[
            {
               "field":"status",
               "value":"ARCHIVED",
               "oldValue":"SUBMITTED"
            },
            {
               "field":"withdrawalReason",
               "value":"ANOTHER_REASON",
               "oldValue":null
            },
            {
               "field":"otherReason",
               "value":"An other reason",
               "oldValue":null
            },
            {
               "field":"withdrawalNote",
               "value":"A withdrawal note",
               "oldValue":null
            }
         ]
      },
      ${expectedCreateTimelineEntry(externalReferralId, caseId, crn, createCommitTime)}
   ]
}
""".trimIndent()

private fun expectedCreateTimelineEntry(
  externalReferralId: UUID,
  caseId: UUID,
  crn: String,
  createCommitTime: String,
  referenceNumber: String = "REF-001",
  submissionNote: String? = null,
): String {
  val submissionNoteChange = submissionNote?.let {
    ",{\"field\":\"submissionNote\",\"value\":\"$it\",\"oldValue\":null}"
  }.orEmpty()
  return """
{
   "type":"CREATE",
   "author":"Delius User",
   "authorDetails":{
      "forename":"Delius",
      "surname":"User",
      "username":"DELIUS_USER"
   },
   "commitDate":"$createCommitTime",
   "changes":[
      {"field":"id","value":"$externalReferralId","oldValue":null},
      {"field":"crn","value":"$crn","oldValue":null},
      {"field":"caseId","value":"$caseId","oldValue":null},
      {"field":"referenceNumber","value":"$referenceNumber","oldValue":null},
      {"field":"submissionDate","value":"2026-01-15","oldValue":null},
      {"field":"status","value":"SUBMITTED","oldValue":null},
      {"field":"organisationName","value":"Organisation name","oldValue":null},
      {"field":"website","value":"https://www.charity.org","oldValue":null}$submissionNoteChange
   ]
}
  """.trimIndent()
}
