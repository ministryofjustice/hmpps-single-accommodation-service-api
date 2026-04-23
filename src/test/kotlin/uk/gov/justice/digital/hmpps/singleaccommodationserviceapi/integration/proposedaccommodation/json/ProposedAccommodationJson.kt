package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.proposedaccommodation.json

import java.util.UUID

fun expectedGetProposedAccommodationsResponse(
  firstId: UUID,
  firstCreatedAt: String,
  secondId: UUID,
  secondCreatedAt: String,
  crn: String,
): String = """
{
  "data": [
  {
    "id" : "$firstId",
    "crn":"$crn",
    "name" : "Test Accommodation",
    "accommodationType": {
      "code": "A07B",
      "description": "Living in the home of a friend, family member or partner: settled"
    },
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
    "crn":"$crn",
    "name" : "Test Accommodation",
    "accommodationType": {
      "code": "A07B",
      "description": "Living in the home of a friend, family member or partner: settled"
    },
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
}
""".trimIndent()

fun expectedGetProposedAccommodationByIdResponse(
  id: UUID,
  crn: String,
  createdAt: String,
): String = """
{
  "data": {
  "id" : "$id",
  "crn": "$crn",
  "name" : "Test Accommodation",
  "accommodationType": {
    "code": "A07B",
    "description": "Living in the home of a friend, family member or partner: settled"
  },
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
}
""".trimIndent()

fun proposedAddressesRequestBody(
  accommodationTypeCode: String,
  verificationStatus: String,
  nextAccommodationStatus: String,
  subBuildingName: String? = "test sub building name",
  postcode: String = "test postcode",
  startDate: String? = "2026-01-05",
  endDate: String? = "2026-04-25",
) = """
  {
    "name" : "Mother's caravan",
    "accommodationTypeCode" : "$accommodationTypeCode",
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
  crn: String,
  accommodationTypeCode: String,
  accommodationTypeDescription: String,
  verificationStatus: String,
  nextAccommodationStatus: String,
  createdBy: String,
  createdAt: String,
): String = """
{
  "id" : "$id",
  "crn":"$crn",
  "name" : "Mother's caravan",
  "accommodationType" : {
    "code": "$accommodationTypeCode",
    "description": "$accommodationTypeDescription"
  },
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
