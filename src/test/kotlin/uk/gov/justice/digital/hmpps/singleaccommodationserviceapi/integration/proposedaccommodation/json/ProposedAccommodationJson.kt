package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.proposedaccommodation.json

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.NextAccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.VerificationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.SingleAccommodationServiceDomainEventType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationTypeEntity
import java.time.LocalDate
import java.util.UUID

fun expectedGetProposedAccommodationsResponse(
  firstId: UUID,
  firstAccommodationTypeEntity: AccommodationTypeEntity,
  firstVerificationStatus: VerificationStatus,
  firstNextAccommodationStatus: NextAccommodationStatus,
  firstStartDate: LocalDate?,
  firstCreatedAt: String,
  firstCreatedBy: String,
  firstBuildingNumber: String,
  secondId: UUID,
  secondAccommodationTypeEntity: AccommodationTypeEntity,
  secondVerificationStatus: VerificationStatus,
  secondNextAccommodationStatus: NextAccommodationStatus,
  secondStartDate: LocalDate?,
  secondCreatedAt: String,
  secondCreatedBy: String,
  crn: String,
): String {
  val firstStartDateString = firstStartDate?.let {
    """
    "$firstStartDate"
    """.trimIndent()
  } ?: "null"
  val secondStartDateString = secondStartDate?.let {
    """
    "$secondStartDate"
    """.trimIndent()
  } ?: "null"
  return """
{
  "data": [
  {
    "id" : "$firstId",
    "crn":"$crn",
    "name" : null,
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
    "startDate" : $firstStartDateString,
    "endDate" : null,
    "createdBy":"$firstCreatedBy",
    "createdAt" : "$firstCreatedAt"
  },
  {
    "id" : "$secondId",
    "crn":"$crn",
    "name" : null,
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
      "country" : "England",
      "uprn" : null
    },
    "startDate" : $secondStartDateString,
    "endDate" : null,
    "createdBy":"$secondCreatedBy",
    "createdAt" : "$secondCreatedAt"
  }
  ]
}
  """.trimIndent()
}

fun expectedGetProposedAccommodationsResponse(
  firstId: UUID,
  firstBuildingNumber: String,
  firstAccommodationTypeEntity: AccommodationTypeEntity,
  firstVerificationStatus: VerificationStatus,
  firstNextAccommodationStatus: NextAccommodationStatus,
  firstStartDate: LocalDate?,
  firstCreatedAt: String,
  firstCreatedBy: String,
  crn: String,
): String = """
    {
      "data": [{
        "id" : "$firstId",
        "crn":"$crn",
        "name" : null,
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
        "startDate" : ${convertNullable(firstStartDate?.toString())},
        "endDate" : null,
        "createdBy":"$firstCreatedBy",
        "createdAt" : "$firstCreatedAt"
      }]
    }
""".trimIndent()

fun expectedGetProposedAccommodationsEmptyResponseWithGetDeliusCaseFailure(): String = """
    {
       "data":[],
       "upstreamFailures":[
          {
             "endpoint":"getCaseByCrn",
             "failureType":"UPSTREAM_HTTP_ERROR",
             "httpResponseStatus":"500 INTERNAL_SERVER_ERROR",
             "message":"500 Internal Server Error: [no body]",
             "identifier":null
          }
       ]
    }
""".trimIndent()

fun expectedGetProposedAccommodationByIdResponse(
  id: UUID,
  crn: String,
  createdAt: String,
  startDate: String,
): String = """
{
  "data": {
  "id" : "$id",
  "crn": "$crn",
  "name" : null,
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
  "startDate" : "$startDate",
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
  buildingName: String? = "test building name",
  buildingNumber: String? = "4",
  thoroughfareName: String? = "test thoroughfare",
  dependentLocality: String? = "test dependent locality",
  postTown: String? = "test post town",
  county: String? = "test county",
  country: String? = "England",
  postcode: String = "test postcode",
  uprn: String? = "test uprn",
  startDate: String? = "2026-01-05",
  endDate: String? = "2026-04-25",
) = """
  {
    "name" : null,
    "accommodationTypeCode" : "$accommodationTypeCode",
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
    },
    "startDate" : ${convertNullable(startDate)},
    "endDate" : ${convertNullable(endDate)}
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
  "name" : null,
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
  "startDate" : "2026-01-05",
  "endDate" : "2026-04-25",
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
