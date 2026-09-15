package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.otheraccommodationreferral.json

import java.util.UUID

fun expectedGetOtherAccommodationReferralTimelineResponse(
  otherAccommodationReferralId: UUID,
  caseId: UUID,
  crn: String,
  localAuthorityAreaId: UUID,
  localAuthorityAreaName: String,
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
               "field":"localAuthorityAreaId",
               "value":"$localAuthorityAreaId",
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
         ],
         "extraInformation":{
            "localAuthorityAreaName":"$localAuthorityAreaName"
         }
      }
   ]
}
""".trimIndent()

@Suppress("LongParameterList")
fun expectedGetOtherAccommodationReferralTimelineResponse(
  otherAccommodationReferralId: UUID,
  caseId: UUID,
  crn: String,
  initialLocalAuthorityAreaId: UUID,
  initialLocalAuthorityAreaName: String,
  updatedLocalAuthorityAreaId: UUID,
  updatedLocalAuthorityAreaName: String,
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
               "field":"localAuthorityAreaId",
               "value":"$updatedLocalAuthorityAreaId",
               "oldValue":"$initialLocalAuthorityAreaId"
            },
            {
               "field":"referenceNumber",
               "value":"REF-002",
               "oldValue":"REF-001"
            }
         ],
         "extraInformation":{
            "localAuthorityAreaName":"$updatedLocalAuthorityAreaName"
         }
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
               "field":"localAuthorityAreaId",
               "value":"$initialLocalAuthorityAreaId",
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
         ],
         "extraInformation":{
            "localAuthorityAreaName":"$initialLocalAuthorityAreaName"
         }
      }
   ]
}
""".trimIndent()
