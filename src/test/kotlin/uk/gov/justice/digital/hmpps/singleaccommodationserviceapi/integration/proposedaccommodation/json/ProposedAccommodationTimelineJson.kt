package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.proposedaccommodation.json

import java.util.UUID

fun expectedGetProposedAccommodationTimelineResponse(
  proposedAccommodationId: UUID,
  caseId: UUID,
  createCommitTime: String,
  update1CommitTime: String,
  update2CommitTime: String,
): String = """
[
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
            "field":"postcode",
            "value":"correct postcode",
            "oldValue":"test postcode"
         }
      ]
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
         }
      ]
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
            "value":"test sub building name"
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
      ]
   }
]
""".trimIndent()
