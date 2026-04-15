package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.proposedaccommodation.json

import java.util.UUID

fun expectedGetProposedAccommodationsResponse(
  firstId: UUID,
  firstCreatedAt: String,
  secondId: UUID,
  secondCreatedAt: String,
  crn: String,
  caseId: UUID,
): String = """
[
  {
    "id" : "$firstId",
    "caseId" : "$caseId",
    "crn":"$crn",
    "name" : "Test Accommodation",
    "arrangementType" : "PRIVATE",
    "arrangementSubType" : "FRIENDS_OR_FAMILY",
    "arrangementSubTypeDescription" : null,
    "settledType" : "SETTLED",
    "offenderReleaseType" : null,
    "verificationStatus" : "NOT_CHECKED_YET",
    "nextAccommodationStatus" : "TO_BE_DECIDED",
    "address" : {
      "postcode" : "W1 8XX",
      "subBuildingName" : null,
      "buildingName" : null,
      "buildingNumber" : "11",
      "thoroughfareName" : "Piccadilly Circus",
      "dependentLocality" : null,
      "postTown" : "London",
      "county" : null,
      "country" : "England",
      "uprn" : null
    },
    "startDate" : null,
    "endDate" : null,
    "createdBy":"Test Data Setup User",
    "createdAt" : "$firstCreatedAt"
  },
  {
    "id" : "$secondId",
    "caseId" : "$caseId", 
    "crn":"$crn",
    "name" : "Test Accommodation",
    "arrangementType" : "PRIVATE",
    "arrangementSubType" : "FRIENDS_OR_FAMILY",
    "arrangementSubTypeDescription" : null,
    "settledType" : "SETTLED",
    "offenderReleaseType" : null,
    "verificationStatus" : "NOT_CHECKED_YET",
    "nextAccommodationStatus" : "TO_BE_DECIDED",
    "address" : {
      "postcode" : "RG26 5AG",
      "subBuildingName" : null,
      "buildingName" : null,
      "buildingNumber" : "4",
      "thoroughfareName" : "Dollis Green",
      "dependentLocality" : null,
      "postTown" : "Bramley",
      "county" : null,
      "country" : "England",
      "uprn" : null
    },
    "startDate" : null,
    "endDate" : null,
    "createdBy":"Test Data Setup User",
    "createdAt" : "$secondCreatedAt"
  }
]
""".trimIndent()

fun expectedGetProposedAccommodationByIdResponse(
  id: UUID,
  caseId: UUID,
  crn: String,
  createdAt: String,
): String = """
{
  "id" : "$id",
  "caseId" : "$caseId",
  "crn": "$crn",
  "name" : "Test Accommodation",
  "arrangementType" : "PRIVATE",
  "arrangementSubType" : "FRIENDS_OR_FAMILY",
  "arrangementSubTypeDescription" : null,
  "settledType" : "SETTLED",
  "offenderReleaseType" : null,
  "verificationStatus" : "NOT_CHECKED_YET",
  "nextAccommodationStatus" : "TO_BE_DECIDED",
  "address" : {
    "postcode" : "W1 8XX",
    "subBuildingName" : null,
    "buildingName" : null,
    "buildingNumber" : "11",
    "thoroughfareName" : "Piccadilly Circus",
    "dependentLocality" : null,
    "postTown" : "London",
    "county" : null,
    "country" : "England",
    "uprn" : null
  },
  "startDate" : null,
  "endDate" : null,
  "createdBy":"Test Data Setup User",
  "createdAt" : "$createdAt"
}
""".trimIndent()

fun proposedAddressesRequestBody(
  verificationStatus: String,
  nextAccommodationStatus: String,
  subBuildingName: String? = "test sub building name",
  postcode: String = "test postcode",
  startDate: String? = "2026-01-05",
  endDate: String? = "2026-04-25",
) = """
  {
    "name" : "Mother's caravan",
    "arrangementType" : "PRIVATE",
    "arrangementSubType" : "OTHER",
    "arrangementSubTypeDescription" : "Caravan site",
    "settledType" : "SETTLED",
    "offenderReleaseType" : "REMAND",
    "verificationStatus" : "$verificationStatus",
    "nextAccommodationStatus" : "$nextAccommodationStatus",
    "address" : {
      "postcode" : "$postcode",
      "subBuildingName" : "$subBuildingName",
      "buildingName" : "test building name",
      "buildingNumber" : "4",
      "thoroughfareName" : "test thoroughfareName",
      "dependentLocality" : "test dependent locality",
      "postTown" : "test post town",
      "county" : "test county",
      "country" : "test country",
      "uprn" : "UP123454"
    },
    "startDate" : ${convertDate(startDate)},
    "endDate" : ${convertDate(endDate)}
  }
""".trimIndent()

fun convertDate(date: String? = "2026-01-05") = if (date == null) {
  "null"
} else {
  """
    "$date"
  """.trimIndent()
}

fun proposedAccommodationNoteRequestBody(
  note: String,
): String = """
  {
    "note" : "$note"
  }
""".trimIndent()

fun expectedProposedAddressesResponseBody(
  id: UUID,
  caseId: UUID,
  crn: String,
  verificationStatus: String,
  nextAccommodationStatus: String,
  createdBy: String,
  createdAt: String,
): String = """
{
  "id" : "$id",
  "caseId" : "$caseId",
  "crn":"$crn",
  "name" : "Mother's caravan",
  "arrangementType" : "PRIVATE",
  "arrangementSubType" : "OTHER",
  "arrangementSubTypeDescription" : "Caravan site",
  "settledType" : "SETTLED",
  "offenderReleaseType" : "REMAND",
  "verificationStatus" : "$verificationStatus",
  "nextAccommodationStatus" : "$nextAccommodationStatus",
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
  "createdBy":"$createdBy",
  "createdAt" : "$createdAt"
}
""".trimIndent()

fun expectedSasAddressUpdatedDomainEventJson(proposedAccommodationId: UUID) = """
  {
    "aggregateId" : "$proposedAccommodationId",
    "type" : "SAS_ACCOMMODATION_UPDATED"
  }
""".trimIndent()
