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
      "author":"Delius User",
      "commitDate":"$createCommitTime",
      "changes":[
         {
            "field":"id",
            "value":"$proposedAccommodationId",
            "oldValue":null
         },
         {
            "field":"caseId",
            "value":"$caseId",
            "oldValue":null
         },
         {
            "field":"accommodationTypeDescription",
            "value":"$accommodationDescription",
            "oldValue":null
         },
         {
            "field":"verificationStatus",
            "value":"PASSED",
            "oldValue":null
         },
         {
            "field":"nextAccommodationStatus",
            "value":"NO",
            "oldValue":null
         },
         {
            "field":"postcode",
            "value":"test postcode",
            "oldValue":null
         },
         {
            "field":"buildingName",
            "value":"test building name",
            "oldValue":null
         },
         {
            "field":"buildingNumber",
            "value":"4",
            "oldValue":null
         },
         {
            "field":"throughfareName",
            "value":"test thoroughfare",
            "oldValue":null
         },
         {
            "field":"dependentLocality",
            "value":"test dependent locality",
            "oldValue":null
         },
         {
            "field":"postTown",
            "value":"test post town",
            "oldValue":null
         },
         {
            "field":"county",
            "value":"test county",
            "oldValue":null
         },
         {
            "field":"uprn",
            "value": "test uprn",
            "oldValue":null
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
      "author":"Delius User",
      "commitDate":"$update2CommitTime",
      "changes":[
         {
            "field":"verificationStatus",
            "value":"PASSED",
            "oldValue":"FAILED"
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
            "field":"subBuildingName",
            "value":"another sub building name",
            "oldValue":"test sub building name"
         }
      ]
   },
   {
      "type":"NOTE",
      "author":"Delius User",
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
      "commitDate":"$createCommitTime",
      "changes":[
         {
            "field":"id",
            "value":"$proposedAccommodationId",
            "oldValue":null
         },
         {
            "field":"caseId",
            "value":"$caseId",
            "oldValue":null
         },
         {
            "field":"accommodationTypeDescription",
            "value":"$initialAccommodationTypeDescription",
            "oldValue":null
         },
         {
            "field":"verificationStatus",
            "value":"PASSED",
            "oldValue":null
         },
         {
            "field":"nextAccommodationStatus",
            "value":"YES",
            "oldValue":null
         },
         {
            "field":"postcode",
            "value":"$postcode",
            "oldValue":null
         },
         {
            "field":"subBuildingName",
            "value":"test sub building name",
            "oldValue":null
         },
         {
            "field":"buildingName",
            "value":"$buildingName",
            "oldValue":null
         },
         {
            "field":"buildingNumber",
            "value":"$buildingNumber",
            "oldValue":null
         },
         {
            "field":"throughfareName",
            "value":"$thoroughfareName",
            "oldValue":null
         },
         {
            "field":"dependentLocality",
            "value":"$dependentLocality",
            "oldValue":null
         },
         {
            "field":"postTown",
            "value":"$postTown",
            "oldValue":null
         },
         {
            "field":"county",
            "value":"$county",
            "oldValue":null
         },
         {
            "field":"uprn",
            "value":"$uprn",
            "oldValue":null
         }
      ]
   }
  ]
}
""".trimIndent()

fun expectedProposedAccommodationTimeResponseForDeliusOriginAudits(
  proposedAccommodationId: UUID,
  caseId: UUID,
) = """
{
   "data":[
      {
         "type":"UPDATE",
         "author":"nDelius user",
         "commitDate":null,
         "changes":[
            {
               "field":"buildingNumber",
               "value":"15",
               "oldValue":"11"
            }
         ]
      },
      {
         "type":"CREATE",
         "author":"nDelius user",
         "commitDate":null,
         "changes":[
            {
               "field":"id",
               "value":"$proposedAccommodationId",
               "oldValue":null
            },
            {
               "field":"caseId",
               "value":"$caseId",
               "oldValue":null
            },
            {
               "field":"accommodationTypeDescription",
               "value":"Living in the home of a friend, family member or partner: transient",
               "oldValue":null
            },
            {
               "field":"verificationStatus",
               "value":"PASSED",
               "oldValue":null
            },
            {
               "field":"nextAccommodationStatus",
               "value":"YES",
               "oldValue":null
            },
            {
               "field":"postcode",
               "value":"Delius postcode",
               "oldValue":null
            },
            {
               "field":"subBuildingName",
               "value":"Delius subBuildingName",
               "oldValue":null
            },
            {
               "field":"buildingName",
               "value":"Delius buildingName",
               "oldValue":null
            },
            {
               "field":"buildingNumber",
               "value":"11",
               "oldValue":null
            },
            {
               "field":"throughfareName",
               "value":"Delius thoroughfareName",
               "oldValue":null
            },
            {
               "field":"dependentLocality",
               "value":"Delius dependentLocality",
               "oldValue":null
            },
            {
               "field":"postTown",
               "value":"Delius postTown",
               "oldValue":null
            },
            {
               "field":"county",
               "value":"Delius county",
               "oldValue":null
            },
            {
               "field":"uprn",
               "value":"Delius uprn",
               "oldValue":null
            }
         ]
      }
   ]
}
""".trimIndent()

fun expectedProposedAccommodationTimeResponseForDeliusAndSasAudits(
  proposedAccommodationId: UUID,
  caseId: UUID,
  sasCommitDateTime: String,
): String = """
   {
      "data":[
         {
            "type":"UPDATE",
            "author":"Delius User",
            "commitDate":"$sasCommitDateTime",
            "changes":[
               {
                  "field":"buildingNumber",
                  "value":"100",
                  "oldValue":"15"
               }
            ]
         },
         {
            "type":"UPDATE",
            "author":"nDelius user",
            "commitDate":null,
            "changes":[
               {
                  "field":"buildingNumber",
                  "value":"15",
                  "oldValue":"11"
               }
            ]
         },
         {
            "type":"CREATE",
            "author":"nDelius user",
            "commitDate":null,
            "changes":[
               {
                  "field":"id",
                  "value":"$proposedAccommodationId",
                  "oldValue":null
               },
               {
                  "field":"caseId",
                  "value":"$caseId",
                  "oldValue":null
               },
               {
                  "field":"accommodationTypeDescription",
                  "value":"Living in the home of a friend, family member or partner: transient",
                  "oldValue":null
               },
               {
                  "field":"verificationStatus",
                  "value":"PASSED",
                  "oldValue":null
               },
               {
                  "field":"nextAccommodationStatus",
                  "value":"YES",
                  "oldValue":null
               },
               {
                  "field":"postcode",
                  "value":"Delius postcode",
                  "oldValue":null
               },
               {
                  "field":"subBuildingName",
                  "value":"Delius subBuildingName",
                  "oldValue":null
               },
               {
                  "field":"buildingName",
                  "value":"Delius buildingName",
                  "oldValue":null
               },
               {
                  "field":"buildingNumber",
                  "value":"11",
                  "oldValue":null
               },
               {
                  "field":"throughfareName",
                  "value":"Delius thoroughfareName",
                  "oldValue":null
               },
               {
                  "field":"dependentLocality",
                  "value":"Delius dependentLocality",
                  "oldValue":null
               },
               {
                  "field":"postTown",
                  "value":"Delius postTown",
                  "oldValue":null
               },
               {
                  "field":"county",
                  "value":"Delius county",
                  "oldValue":null
               },
               {
                  "field":"uprn",
                  "value":"Delius uprn",
                  "oldValue":null
               }
            ]
         }
      ]
   }
""".trimIndent()
