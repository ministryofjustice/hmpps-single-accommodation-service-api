package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.proposedaccommodation.json

import java.util.UUID

fun expectedGetProposedAccommodationTimelineResponse(
  proposedAccommodationId: UUID,
  caseId: UUID,
  createCommitTime: String,
): String = """
{
  "data": [
   {
      "type":"CREATE",
      "author":"DeliusUser",
      "commitDate":"$createCommitTime",
      "changes":[
         {
            "field":"id",
            "value":"$proposedAccommodationId"
         },
         {
            "field":"caseId",
            "value":"$caseId"
         },
         {
            "field":"name",
            "value":"Mother's caravan"
         },
         {
            "field":"arrangementType",
            "value":"PRIVATE"
         },
         {
            "field":"arrangementSubType",
            "value":"OTHER"
         },
         {
            "field":"arrangementSubTypeDescription",
            "value":"Caravan site"
         },
         {
            "field":"settledType",
            "value":"SETTLED"
         },
         {
            "field":"verificationStatus",
            "value":"PASSED"
         },
         {
            "field":"nextAccommodationStatus",
            "value":"NO"
         },
         {
            "field":"offenderReleaseType",
            "value":"REMAND"
         },
         {
            "field":"startDate",
            "value":"2026-01-05"
         },
         {
            "field":"endDate",
            "value":"2026-04-25"
         },
         {
            "field":"postcode",
            "value":"test postcode"
         },
         {
            "field":"subBuildingName",
            "value":null
         },
         {
            "field":"buildingName",
            "value":"test building name"
         },
         {
            "field":"buildingNumber",
            "value":"4"
         },
         {
            "field":"throughfareName",
            "value":"test thoroughfareName"
         },
         {
            "field":"dependentLocality",
            "value":"test dependent locality"
         },
         {
            "field":"postTown",
            "value":"test post town"
         },
         {
            "field":"county",
            "value":"test county"
         },
         {
            "field":"country",
            "value":"test country"
         },
         {
            "field":"uprn",
            "value":"UP123454"
         }
      ],
      "extraInformation":{}
   }
  ]
}
""".trimIndent()

fun expectedGetProposedAccommodationTimelineResponse(
  proposedAccommodationId: UUID,
  caseId: UUID,
  createCommitTime: String,
  createNoteCommitTime: String,
  update1CommitTime: String,
  update2CommitTime: String,
): String = """
{
  "data": [
   {
      "type":"UPDATE",
      "author":"DeliusUser",
      "commitDate":"$update2CommitTime",
      "changes":[
         {
            "field":"verificationStatus",
            "value":"PASSED",
            "oldValue":"FAILED"
         },
         {
            "field":"nextAccommodationStatus",
            "value":"YES",
            "oldValue":"NO"
         },
         {
            "field":"startDate",
            "value":"2026-01-20",
            "oldValue":null
         },
         {
            "field":"endDate",
            "value":"2026-08-01",
            "oldValue":null
         },
         {
            "field":"postcode",
            "value":"correct postcode",
            "oldValue":"test postcode"
         },
         {
            "field":"subBuildingName",
            "value":null,
            "oldValue":"another sub building name"
         }
      ],
      "extraInformation":{}
   },
   {
      "type":"UPDATE",
      "author":"Nomis User",
      "commitDate":"$update1CommitTime",
      "changes":[
         {
            "field":"verificationStatus",
            "value":"FAILED",
            "oldValue":"PASSED"
         },
         {
            "field":"startDate",
            "value":null,
            "oldValue":"2026-01-05"
         },
         {
            "field":"endDate",
            "value":null,
            "oldValue":"2026-04-25"
         },
         {
            "field":"subBuildingName",
            "value":"another sub building name",
            "oldValue":null
         }
      ],
      "extraInformation":{}
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
            "value":"$proposedAccommodationId"
         },
         {
            "field":"caseId",
            "value":"$caseId"
         },
         {
            "field":"name",
            "value":"Mother's caravan"
         },
         {
            "field":"arrangementType",
            "value":"PRIVATE"
         },
         {
            "field":"arrangementSubType",
            "value":"OTHER"
         },
         {
            "field":"arrangementSubTypeDescription",
            "value":"Caravan site"
         },
         {
            "field":"settledType",
            "value":"SETTLED"
         },
         {
            "field":"verificationStatus",
            "value":"PASSED"
         },
         {
            "field":"nextAccommodationStatus",
            "value":"NO"
         },
         {
            "field":"offenderReleaseType",
            "value":"REMAND"
         },
         {
            "field":"startDate",
            "value":"2026-01-05"
         },
         {
            "field":"endDate",
            "value":"2026-04-25"
         },
         {
            "field":"postcode",
            "value":"test postcode"
         },
         {
            "field":"subBuildingName",
            "value":null
         },
         {
            "field":"buildingName",
            "value":"test building name"
         },
         {
            "field":"buildingNumber",
            "value":"4"
         },
         {
            "field":"throughfareName",
            "value":"test thoroughfareName"
         },
         {
            "field":"dependentLocality",
            "value":"test dependent locality"
         },
         {
            "field":"postTown",
            "value":"test post town"
         },
         {
            "field":"county",
            "value":"test county"
         },
         {
            "field":"country",
            "value":"test country"
         },
         {
            "field":"uprn",
            "value":"UP123454"
         }
      ],
      "extraInformation":{}
   }
  ]
}
""".trimIndent()
