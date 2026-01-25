package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.privateaddress.json

import java.util.UUID

fun expectedGetPrivateAddressesResponse(): String = """
[ {
  "id" : "6d9a38c4-a8f6-49d1-856d-972906f63361",
  "name" : null,
  "arrangementType" : "PRIVATE",
  "arrangementSubType" : "FRIENDS_OR_FAMILY",
  "arrangementSubTypeDescription" : null,
  "settledType" : "SETTLED",
  "offenderReleaseType" : null,
  "status" : "NOT_CHECKED_YET",
  "address" : {
    "postcode" : "RG26 5AG",
    "subBuildingName" : null,
    "buildingName" : null,
    "buildingNumber" : "4",
    "thoroughfareName" : "Dollis Green",
    "dependentLocality" : null,
    "postTown" : "Bramley",
    "county" : null,
    "country" : null,
    "uprn" : null
  },
  "startDate" : null,
  "endDate" : null,
  "createdAt" : "2026-01-08T13:27:15.120069Z"
}, {
  "id" : "f03aac3e-2f36-4003-a753-db571fe140b8",
  "name" : null,
  "arrangementType" : "PRIVATE",
  "arrangementSubType" : "FRIENDS_OR_FAMILY",
  "arrangementSubTypeDescription" : null,
  "settledType" : "SETTLED",
  "offenderReleaseType" : null,
  "status" : "NOT_CHECKED_YET",
  "address" : {
    "postcode" : "W1 8XX",
    "subBuildingName" : null,
    "buildingName" : null,
    "buildingNumber" : "11",
    "thoroughfareName" : "Piccadilly Circus",
    "dependentLocality" : null,
    "postTown" : "London",
    "county" : null,
    "country" : null,
    "uprn" : null
  },
  "startDate" : null,
  "endDate" : null,
  "createdAt" : "2026-01-05T10:07:15.120069Z"
} ]
""".trimIndent()

fun proposedAddressesRequestBody(accommodationStatus: String): String = """
  {
    "name" : "Mother's caravan",
    "arrangementType" : "PRIVATE",
    "arrangementSubType" : "OTHER",
    "arrangementSubTypeDescription" : "Caravan site",
    "settledType" : "SETTLED",
    "offenderReleaseType" : "REMAND",
    "status" : "$accommodationStatus",
    "address" : {
      "postcode" : "test postcode",
      "subBuildingName" : "test sub building name",
      "buildingName" : "test building name",
      "buildingNumber" : "4",
      "thoroughfareName" : "test thoroughfareName",
      "dependentLocality" : "test dependent locality",
      "postTown" : "test post town",
      "county" : "test county",
      "country" : "test country",
      "uprn" : "UP123454"
    },
    "startDate" : "2026-01-05",
    "endDate" : "2026-04-25"
  }
""".trimIndent()

fun expectedProposedAddressesResponseBody(id: UUID, accommodationStatus: String, createdAt: String): String = """
{
  "id" : "$id",
  "name" : "Mother's caravan",
  "arrangementType" : "PRIVATE",
  "arrangementSubType" : "OTHER",
  "arrangementSubTypeDescription" : "Caravan site",
  "settledType" : "SETTLED",
  "offenderReleaseType" : "REMAND",
  "status" : "$accommodationStatus",
  "address" : {
    "postcode" : "test postcode",
    "subBuildingName" : "test sub building name",
    "buildingName" : "test building name",
    "buildingNumber" : "4",
    "thoroughfareName" : "test thoroughfareName",
    "dependentLocality" : "test dependent locality",
    "postTown" : "test post town",
    "county" : "test county",
    "country" : "test country",
    "uprn" : "UP123454"
  },
  "startDate" : "2026-01-05",
  "endDate" : "2026-04-25",
  "createdAt" : "$createdAt"
}
""".trimIndent()

fun expectedSasAddressUpdatedDomainEventJson(proposedAccommodationId: UUID) = """
  {
    "aggregateId" : "$proposedAccommodationId",
    "type" : "SAS_ADDRESS_UPDATED"
  }
""".trimIndent()
