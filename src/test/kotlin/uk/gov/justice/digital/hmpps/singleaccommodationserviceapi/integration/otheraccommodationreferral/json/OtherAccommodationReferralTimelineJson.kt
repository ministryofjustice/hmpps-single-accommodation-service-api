package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.otheraccommodationreferral.json

import java.util.UUID

@Suppress("LongParameterList")
fun expectedGetOtherAccommodationReferralOutcomeTimelineResponse(
  otherAccommodationReferralId: UUID,
  caseId: UUID,
  crn: String,
  createCommitTime: String,
  updateCommitTime: String,
  newStatus: String,
  outcomeReason: String,
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
               "field":"outcomeReason",
               "value":"$outcomeReason",
               "oldValue":null
            },
            {
               "field":"outcomeNote",
               "value":"$outcomeNote",
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
               "field":"crn",
               "value":"$crn",
               "oldValue":null
            },
            {
               "field":"caseId",
               "value":"$caseId",
               "oldValue":null
            },
            {
               "field":"referenceNumber",
               "value":"REF-001",
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
            },
            {
               "field":"organisationName",
               "value":"Organisation name",
               "oldValue":null
            },
            {
               "field":"website",
               "value":"https://www.charity.org",
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
  crn: String,
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
               "field":"crn",
               "value":"$crn",
               "oldValue":null
            },
            {
               "field":"caseId",
               "value":"$caseId",
               "oldValue":null
            },
            {
               "field":"referenceNumber",
               "value":"REF-001",
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
            },
            {
               "field":"organisationName",
               "value":"Organisation name",
               "oldValue":null
            },
            {
               "field":"website",
               "value":"https://www.charity.org",
               "oldValue":null
            },
            {
               "field":"submissionNote",
               "value":"A submission note",
               "oldValue":null
            }
         ]
      }
   ]
}
""".trimIndent()

@Suppress("LongParameterList")
fun expectedGetOtherAccommodationReferralTimelineResponse(
  otherAccommodationReferralId: UUID,
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
               "field":"crn",
               "value":"$crn",
               "oldValue":null
            },
            {
               "field":"caseId",
               "value":"$caseId",
               "oldValue":null
            },
            {
               "field":"referenceNumber",
               "value":"$previousReferenceNumber",
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
            },
            {
               "field":"organisationName",
               "value":"Organisation name",
               "oldValue":null
            },
            {
               "field":"website",
               "value":"https://www.charity.org",
               "oldValue":null
            },
            {
               "field":"submissionNote",
               "value":"A submission note",
               "oldValue":null
            }
         ]
      }
   ]
}
""".trimIndent()
