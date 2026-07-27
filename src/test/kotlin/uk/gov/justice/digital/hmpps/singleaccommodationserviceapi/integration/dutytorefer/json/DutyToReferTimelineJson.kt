package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.dutytorefer.json

import java.util.UUID

fun expectedGetDutyToReferTimelineResponse(
  dutyToReferId: UUID,
  caseId: UUID,
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
            "username":"DELIUS_USER",
            "staffCode":null
         },
         "commitDate":"$createCommitTime",
         "changes":[
            {
               "field":"id",
               "value":"$dutyToReferId",
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
               "value":"DTR-REF-001",
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
         ],
         "extraInformation":{
            "localAuthorityAreaName":"$localAuthorityAreaName"
         }
      }
   ]
}
""".trimIndent()

fun expectedGetDutyToReferTimelineResponse(
  dutyToReferId: UUID,
  caseId: UUID,
  initialLocalAuthorityAreaId: UUID,
  initialLocalAuthorityAreaName: String,
  updatedLocalAuthorityAreaId: UUID,
  updatedLocalAuthorityAreaName: String,
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
            "username":"DELIUS_USER",
            "staffCode":null
         },
         "commitDate":"$update2CommitTime",
         "changes":[
            {
               "field":"localAuthorityAreaId",
               "value":"$updatedLocalAuthorityAreaId",
               "oldValue":"$initialLocalAuthorityAreaId"
            },
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
         ],
         "extraInformation":{
            "localAuthorityAreaName":"$updatedLocalAuthorityAreaName"
         }
      },
      {
         "type":"UPDATE",
         "author":"Delius User",
         "authorDetails":{
            "forename":"Delius",
            "surname":"User",
            "username":"DELIUS_USER",
            "staffCode":null
         },
         "commitDate":"$update1CommitTime",
         "changes":[
            {
               "field":"referenceNumber",
               "value":"DTR-REF-002",
               "oldValue":"DTR-REF-001"
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
         ],
         "extraInformation":{
            "localAuthorityAreaName":"$initialLocalAuthorityAreaName"
         }
      },
      {
         "type":"NOTE",
         "author":"Delius User",
         "authorDetails":{
            "forename":"Delius",
            "surname":"User",
            "username":"DELIUS_USER",
            "staffCode":null
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
            "username":"DELIUS_USER",
            "staffCode":null
         },
         "commitDate":"$createCommitTime",
         "changes":[
            {
               "field":"id",
               "value":"$dutyToReferId",
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
               "value":"DTR-REF-001",
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
         ],
         "extraInformation":{
            "localAuthorityAreaName":"$initialLocalAuthorityAreaName"
         }
      }
   ]
}
""".trimIndent()
