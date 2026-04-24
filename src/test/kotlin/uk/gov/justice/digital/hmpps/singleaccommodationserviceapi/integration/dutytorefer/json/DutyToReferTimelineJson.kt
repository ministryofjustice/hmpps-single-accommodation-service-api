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
         "author":"DeliusUser",
         "commitDate":"$createCommitTime",
         "changes":[
            {
               "field":"id",
               "value":"$dutyToReferId"
            },
            {
               "field":"caseId",
               "value":"$caseId"
            },
            {
               "field":"localAuthorityAreaId",
               "value":"$localAuthorityAreaId"
            },
            {
               "field":"referenceNumber",
               "value":"DTR-REF-001"
            },
            {
               "field":"submissionDate",
               "value":"2026-01-15"
            },
            {
               "field":"status",
               "value":"SUBMITTED"
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
         "author":"DeliusUser",
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
            }
         ],
         "extraInformation":{
            "localAuthorityAreaName":"$updatedLocalAuthorityAreaName"
         }
      },
      {
         "type":"UPDATE",
         "author":"Nomis User",
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
            }
         ],
         "extraInformation":{
            "localAuthorityAreaName":"$initialLocalAuthorityAreaName"
         }
      },
      {
         "type":"NOTE",
         "author":"DeliusUser",
         "commitDate":"$createNoteCommitTime",
         "changes":[
            {
               "field":"note",
               "value":"Test note"
            }
         ],
         "extraInformation":{}
      },
      {
         "type":"CREATE",
         "author":"DeliusUser",
         "commitDate":"$createCommitTime",
         "changes":[
            {
               "field":"id",
               "value":"$dutyToReferId"
            },
            {
               "field":"caseId",
               "value":"$caseId"
            },
            {
               "field":"localAuthorityAreaId",
               "value":"$initialLocalAuthorityAreaId"
            },
            {
               "field":"referenceNumber",
               "value":"DTR-REF-001"
            },
            {
               "field":"submissionDate",
               "value":"2026-01-15"
            },
            {
               "field":"status",
               "value":"SUBMITTED"
            }
         ],
         "extraInformation":{
            "localAuthorityAreaName":"$initialLocalAuthorityAreaName"
         }
      }
   ]
}
""".trimIndent()
