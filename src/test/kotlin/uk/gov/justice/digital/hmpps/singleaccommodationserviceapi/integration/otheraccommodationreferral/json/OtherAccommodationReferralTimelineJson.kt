package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.otheraccommodationreferral.json

import java.util.UUID

fun expectedGetOtherAccommodationReferralTimelineResponse(
  otherAccommodationReferralId: UUID,
  caseId: UUID,
  createCommitTime: String,
): String = """
{
   "data":[
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
            {
               "field":"id",
               "value":"$otherAccommodationReferralId",
               "oldValue":null
            },
            {
               "field":"caseId",
               "value":"$caseId",
               "oldValue":null
            },
            {
               "field":"referenceNumber",
               "value":"OOR-REF-001",
               "oldValue":null
            },
            {
               "field":"submissionDate",
               "value":"2026-01-15",
               "oldValue":null
            },
            {
               "field":"status",
               "value":"SUBMITTED",
               "oldValue":null
            }
         ]
      }
   ]
}
""".trimIndent()

fun expectedGetOtherAccommodationReferralTimelineResponse(
  otherAccommodationReferralId: UUID,
  caseId: UUID,
  createCommitTime: String,
  createNoteCommitTime: String,
  update1CommitTime: String,
  update2CommitTime: String,
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
         "commitDate":"$update2CommitTime",
         "changes":[
            {
               "field":"status",
               "value":"ACCEPTED",
               "oldValue":"NOT_ACCEPTED"
            },
            {
               "field":"outcomeReason",
               "value":"PRIORITY_NEED",
               "oldValue":"NO_LOCAL_CONNECTION"
            }
         ]
      },
      {
         "type":"UPDATE",
         "author":"Delius User",
         "authorDetails":{
            "forename":"Delius",
            "surname":"User",
            "username":"DELIUS_USER"
         },
         "commitDate":"$update1CommitTime",
         "changes":[
            {
               "field":"referenceNumber",
               "value":"OOR-REF-002",
               "oldValue":"OOR-REF-001"
            },
            {
               "field":"status",
               "value":"NOT_ACCEPTED",
               "oldValue":"SUBMITTED"
            },
            {
               "field":"outcomeReason",
               "value":"NO_LOCAL_CONNECTION",
               "oldValue":null
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
            {
               "field":"id",
               "value":"$otherAccommodationReferralId",
               "oldValue":null
            },
            {
               "field":"caseId",
               "value":"$caseId",
               "oldValue":null
            },
            {
               "field":"referenceNumber",
               "value":"OOR-REF-001",
               "oldValue":null
            },
            {
               "field":"submissionDate",
               "value":"2026-01-15",
               "oldValue":null
            },
            {
               "field":"status",
               "value":"SUBMITTED",
               "oldValue":null
            }
         ]
      }
   ]
}
""".trimIndent()
