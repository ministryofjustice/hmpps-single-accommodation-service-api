package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.proposedaccommodation.json

import java.util.UUID

fun expectedGetProposedAccommodationTimelineResponse(
  proposedAccommodationId: UUID,
  accommodationDescription: String,
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
            "field":"accommodationTypeDescription",
            "value":"$accommodationDescription"
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
            "value":"test thoroughfare"
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
            "value":"England"
         },
         {
            "field":"uprn",
            "value": "test uprn"
         }
      ]
   }
  ]
}
""".trimIndent()

fun expectedGetProposedAccommodationTimelineResponse(
  proposedAccommodationId: UUID,
  caseId: UUID,
  buildingName: String? = "test building name",
  buildingNumber: String? = "4",
  thoroughfareName: String? = "test thoroughfare",
  dependentLocality: String? = "test dependent locality",
  postTown: String? = "test post town",
  county: String? = "test county",
  country: String? = "England",
  postcode: String = "test postcode",
  uprn: String? = "test uprn",
  initialAccommodationTypeDescription: String,
  updatedAccommodationTypeDescription: String,
  createCommitTime: String,
  createNoteCommitTime: String,
  update1Author: String,
  update1CommitTime: String,
  update2CommitTime: String,
) = """
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
      ]
   },
   {
      "type":"UPDATE",
      "author":"$update1Author",
      "commitDate":"$update1CommitTime",
      "changes":[
         {
            "field":"accommodationTypeDescription",
            "value":"$updatedAccommodationTypeDescription",
            "oldValue":"$initialAccommodationTypeDescription"
         },
         {
            "field":"verificationStatus",
            "value":"FAILED",
            "oldValue":"PASSED"
         },
         {
            "field": "nextAccommodationStatus",
            "oldValue": "YES",
            "value": "NO"
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
            "oldValue":"test sub building name"
         }
      ]
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
            "field":"accommodationTypeDescription",
            "value":"$initialAccommodationTypeDescription"
         },
         {
            "field":"verificationStatus",
            "value":"PASSED"
         },
         {
            "field":"nextAccommodationStatus",
            "value":"YES"
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
            "value":"$postcode"
         },
         {
            "field":"subBuildingName",
            "value":"test sub building name"
         },
         {
            "field":"buildingName",
            "value":"$buildingName"
         },
         {
            "field":"buildingNumber",
            "value":"$buildingNumber"
         },
         {
            "field":"throughfareName",
            "value":"$thoroughfareName"
         },
         {
            "field":"dependentLocality",
            "value":"$dependentLocality"
         },
         {
            "field":"postTown",
            "value":"$postTown"
         },
         {
            "field":"county",
            "value":"$county"
         },
         {
            "field":"country",
            "value":"$country"
         },
         {
            "field":"uprn",
            "value":"$uprn"
         }
      ]
   }
  ]
}
""".trimIndent()
