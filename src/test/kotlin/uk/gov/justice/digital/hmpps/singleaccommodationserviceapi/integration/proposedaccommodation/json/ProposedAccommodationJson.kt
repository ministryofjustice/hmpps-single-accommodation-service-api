package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.proposedaccommodation.json

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.NextAccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.VerificationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.SingleAccommodationServiceDomainEventType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationTypeEntity
import java.util.UUID

fun expectedGetProposedAccommodationsEmptyListResponse(): String = """
{
  "data": [
  ]
}
""".trimIndent()

fun expectedGetProposedAccommodationsResponse(
  firstId: UUID,
  firstAccommodationTypeEntity: AccommodationTypeEntity,
  firstVerificationStatus: VerificationStatus,
  firstNextAccommodationStatus: NextAccommodationStatus,
  firstCreatedAt: String,
  firstCreatedBy: String,
  firstBuildingNumber: String,
  secondId: UUID,
  secondAccommodationTypeEntity: AccommodationTypeEntity,
  secondVerificationStatus: VerificationStatus,
  secondNextAccommodationStatus: NextAccommodationStatus,
  secondCreatedAt: String,
  secondCreatedBy: String,
  crn: String,
) = """
{
  "data": [
  {
    "id" : "$firstId",
    "crn":"$crn",
    "accommodationType": {
      "code": "${firstAccommodationTypeEntity.code}",
      "description": "${firstAccommodationTypeEntity.name}"
    },
    "verificationStatus" : "${firstVerificationStatus.name}",
    "nextAccommodationStatus" : "${firstNextAccommodationStatus.name}",
    "address" : {
      "postcode" : "W1 8XX",
      "subBuildingName" : null,
      "buildingName" : null,
      "buildingNumber" : "$firstBuildingNumber",
      "thoroughfareName" : "Piccadilly Circus",
      "dependentLocality" : null,
      "postTown" : "London",
      "county" : null,
      "country" : null,
      "uprn" : null
    },
    "createdBy":"$firstCreatedBy",
    "createdAt" : "$firstCreatedAt"
  },
  {
    "id" : "$secondId",
    "crn":"$crn",
    "accommodationType": {
      "code": "${secondAccommodationTypeEntity.code}",
      "description": "${secondAccommodationTypeEntity.name}"
    },
    "verificationStatus" : "${secondVerificationStatus.name}",
    "nextAccommodationStatus" : "${secondNextAccommodationStatus.name}",
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
    "createdBy":"$secondCreatedBy",
    "createdAt" : "$secondCreatedAt"
  }
  ]
}
""".trimIndent()

fun expectedGetProposedAccommodationsResponse(
  expectedId: UUID,
  expectedPostcode: String,
  expectedSubBuildingName: String,
  expectedBuildingName: String,
  expectedBuildingNumber: String,
  expectedThoroughfareName: String,
  expectedDependentLocality: String,
  expectedPostTown: String,
  expectedCounty: String,
  expectedUprn: String,
  expectedAccommodationTypeEntity: AccommodationTypeEntity?,
  expectedVerificationStatus: VerificationStatus,
  expectedNextAccommodationStatus: NextAccommodationStatus,
  expectedCreatedAt: String,
  expectedCreatedBy: String,
  crn: String,
): String {
  val expectedAccommodationType = expectedAccommodationTypeEntity?.let {
    """
    {
      "code": "${expectedAccommodationTypeEntity.code}",
      "description": "${expectedAccommodationTypeEntity.name}"
    }
    """.trimIndent()
  } ?: """null"""
  return """
    {
      "data": [{
        "id" : "$expectedId",
        "crn":"$crn",
        "accommodationType": $expectedAccommodationType,
        "verificationStatus" : "${expectedVerificationStatus.name}",
        "nextAccommodationStatus" : "${expectedNextAccommodationStatus.name}",
        "address" : {
          "postcode" : "$expectedPostcode",
          "subBuildingName" : "$expectedSubBuildingName",
          "buildingName" : "$expectedBuildingName",
          "buildingNumber" : "$expectedBuildingNumber",
          "thoroughfareName" : "$expectedThoroughfareName",
          "dependentLocality" : "$expectedDependentLocality",
          "postTown" : "$expectedPostTown",
          "county" : "$expectedCounty",
          "country" : null,
          "uprn" : "$expectedUprn"
        },
        "createdBy":"$expectedCreatedBy",
        "createdAt" : "$expectedCreatedAt"
      }]
    }
  """.trimIndent()
}

fun expectedGetProposedAccommodationsEmptyResponse() = """
    {
      "data": []
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
  "accommodationType": {
    "code": "A07B",
    "description": "Living in the home of a friend, family member or partner: settled"
  },
  "verificationStatus" : "PASSED",
  "nextAccommodationStatus" : "YES",
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
  "createdBy":"Test Data Setup User",
  "createdAt" : "$createdAt"
  }
}
""".trimIndent()

fun proposedAddressesRequestBody(
  accommodationTypeCode: String?,
  verificationStatus: String,
  nextAccommodationStatus: String,
  subBuildingName: String? = "test sub building name",
  buildingName: String? = "test building name",
  buildingNumber: String? = "4",
  thoroughfareName: String? = "test thoroughfare",
  dependentLocality: String? = "test dependent locality",
  postTown: String? = "test post town",
  county: String? = "test county",
  country: String? = "England",
  postcode: String = "test postcode",
  uprn: String? = "test uprn",
): String {
  val accommodationTypeConverted = accommodationTypeCode?.let {
    """
      "$accommodationTypeCode"
    """.trimIndent()
  } ?: """ null """
  return """
  {
    "accommodationTypeCode" : $accommodationTypeConverted,
    "verificationStatus" : "$verificationStatus",
    "nextAccommodationStatus" : "$nextAccommodationStatus",
    "address" : {
      "postcode" : "$postcode",
      "subBuildingName" : ${convertNullable(subBuildingName)},
      "buildingName" : ${convertNullable(buildingName)},
      "buildingNumber" : ${convertNullable(buildingNumber)},
      "thoroughfareName" : ${convertNullable(thoroughfareName)},
      "dependentLocality" :${convertNullable(dependentLocality)},
      "postTown" : ${convertNullable(postTown)},
      "county" : ${convertNullable(county)},
      "country" : ${convertNullable(country)},
      "uprn" : ${convertNullable(uprn)}
    }
  }
  """.trimIndent()
}

fun proposedAccommodationArrivalRequestBody(
  arrivalDate: String = "2026-01-05",
) = """
  {
    "arrivalDate": "$arrivalDate"
  }
""".trimIndent()

fun convertNullable(nullable: String?) = if (nullable == null) {
  "null"
} else {
  """
    "$nullable"
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
  postcode: String,
  subBuildingName: String,
  buildingName: String,
  buildingNumber: String,
  thoroughfareName: String,
  dependentLocality: String,
  postTown: String,
  county: String,
  uprn: String,
  createdBy: String,
  createdAt: String,
): String = """
{
  "id" : "$id",
  "crn":"$crn",
  "accommodationType" : {
    "code": "$accommodationTypeCode",
    "description": "$accommodationTypeDescription"
  },
  "verificationStatus" : "$verificationStatus",
  "nextAccommodationStatus" : "$nextAccommodationStatus",
  "address" : {
    "postcode" : "$postcode",
    "subBuildingName" : "$subBuildingName",
    "buildingName" : "$buildingName",
    "buildingNumber" : "$buildingNumber",
    "thoroughfareName" : "$thoroughfareName",
    "dependentLocality" : "$dependentLocality",
    "postTown" : "$postTown",
    "county" : "$county",
    "country" : "England",
    "uprn" : "$uprn"
  },
  "createdBy":"$createdBy",
  "createdAt" : "$createdAt"
}
""".trimIndent()

fun expectedSasAddressUpdatedDomainEventJson(
  proposedAccommodationId: UUID,
  cprAddressId: UUID,
  eventType: SingleAccommodationServiceDomainEventType,
) = """
  {
    "aggregateId" : "$proposedAccommodationId",
    "cprAddressId":"$cprAddressId",
    "type" : "${eventType.name}"
  }
""".trimIndent()
