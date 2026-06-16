package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.accommodation.json

import java.util.UUID

fun expectedGetAccommodationHistoryResponse(): String = """
{
   "data":[
      {
         "crn":"FAKECRN",
         "startDate":"2025-10-17",
         "endDate":"2026-10-17",
         "address":{
            "postcode":"SW1A 1AA",
            "subBuildingName":null,
            "buildingName":null,
            "buildingNumber":"1",
            "thoroughfareName":"Some Street",
            "dependentLocality":null,
            "postTown":"London",
            "county":null,
            "country":null,
            "uprn":null
         },
         "status": {
            "code":"M",
            "description":"Main"
         },
         "type": {
            "code":"A07A",
            "description":"Friends/Family (transient)"
         }
      },
      {
         "crn":"FAKECRN",
         "startDate":"2024-10-17",
         "endDate":"2025-10-17",
         "address":{
            "postcode":null,
            "subBuildingName":null,
            "buildingName":null,
            "buildingNumber":"1",
            "thoroughfareName":null,
            "dependentLocality":null,
            "postTown":null,
            "county":null,
            "country":null,
            "uprn":null
         },
         "status":{
            "code":"P",
            "description":"Previous"
         },
         "type":{
            "code":"A08A",
            "description":"Homeless - Rough Sleeping"
         }
      }
   ]
}
""".trimIndent()

fun expectedGetAccommodationHistoryWithUpstreamFailureResponse(): String = """
{
   "data":[],
   "upstreamFailures":[
      {
         "endpoint":"getCorePersonRecordByCrn",
         "failureType":"UPSTREAM_HTTP_ERROR",
         "httpResponseStatus":"500 INTERNAL_SERVER_ERROR",
         "identifier":null,
         "message":"500 Internal Server Error: [no body]"
      }
   ]
}
""".trimIndent()

fun expectedGetCurrentAccommodationResponse(crn: String): String = """
{
   "data":{
      "crn":"$crn",
      "startDate":"2026-01-11",
      "endDate":null,
      "address":{
         "postcode":"SW1A 1AA",
         "subBuildingName":null,
         "buildingName":null,
         "buildingNumber":"1",
         "thoroughfareName":"Some Street",
         "dependentLocality":null,
         "postTown":"London",
         "county":null,
         "country":null,
         "uprn":null
      },
      "status":{
         "code":"M",
         "description":"Main"
      },
      "type":{
         "code":"A07B",
         "description":"Friends/Family (settled)"
      }
   }
}
""".trimIndent()

fun expectedGetCurrentAccommodationPrisonResponse(crn: String): String = """
{
   "data":{
      "crn":"$crn",
      "startDate":null,
      "endDate":"2025-10-17",
      "address":{
         "postcode":null,
         "subBuildingName":null,
         "buildingName":"Wandsworth",
         "buildingNumber":null,
         "thoroughfareName":null,
         "dependentLocality":null,
         "postTown":null,
         "county":null,
         "country":null,
         "uprn":null
      },
      "status":{
         "code":"C",
         "description":"Custody"
      },
      "type":{
         "code":"HMP",
         "description":"Wandsworth"
      }
   }
}
""".trimIndent()

fun expectedGetCurrentAccommodationCas1CurrentPremisesResponse(
  crn: String,
  startDate: String,
  endDate: String,
  postcode: String,
  dependentLocality: String,
  thoroughfareName: String,
  postTown: String,
): String = """
{
   "data":{
      "crn":"$crn",
      "startDate":"$startDate",
      "endDate":"$endDate",
      "address":{
         "postcode":"$postcode",
         "subBuildingName":null,
         "buildingName":null,
         "buildingNumber":null,
         "thoroughfareName":"$thoroughfareName",
         "dependentLocality":"$dependentLocality",
         "postTown":"$postTown",
         "county":null,
         "country":null,
         "uprn":null
      },
      "status":{
         "code":"M",
         "description":"Main"
      },
      "type":{
         "code":"A02",
         "description":"Approved Premises"
      }
   }
}
""".trimIndent()

fun expectedGetCurrentAccommodationCas3CurrentPremisesResponse(
  crn: String,
  startDate: String,
  endDate: String,
  postcode: String,
  dependentLocality: String,
  thoroughfareName: String,
  postTown: String,
): String = """
{
   "data":{
      "crn":"$crn",
      "startDate":"$startDate",
      "endDate":"$endDate",
      "address":{
         "postcode":"$postcode",
         "subBuildingName":null,
         "buildingName":null,
         "buildingNumber":null,
         "thoroughfareName":"$thoroughfareName",
         "dependentLocality":"$dependentLocality",
         "postTown":"$postTown",
         "county":null,
         "country":null,
         "uprn":null
      },
      "status":{
         "code":"M",
         "description":"Main"
      },
      "type":{
         "code":"A17",
         "description":"CAS3"
      }
   }
}
""".trimIndent()

fun expectedGetNextAccommodationsResponse(
  prStartDate: String,
  prEndDate: String,
  crn: String,
): String = """
{
   "data":
      {
         "crn":"$crn",
         "startDate":"$prStartDate",
         "endDate":"$prEndDate",
         "address":{
            "postcode":"W5 2AB",
            "subBuildingName":null,
            "buildingName":null,
            "buildingNumber":"1",
            "thoroughfareName":"Another Street",
            "dependentLocality":null,
            "postTown":"London",
            "county":null,
            "country":null,
            "uprn":null
         },
         "status":{
            "code":"PR",
            "description":"Proposed"
         },
         "type":{
            "code":"A07A",
            "description":"Friends/Family (transient)"
         }
      }
}
""".trimIndent()

fun expectedGetCurrentAccommodationWithAllUpstreamFailureResponse(): String = """
{
   "data":null,
   "upstreamFailures":[
      {
         "endpoint":"getCorePersonRecordByCrn",
         "failureType":"UPSTREAM_HTTP_ERROR",
         "httpResponseStatus":"500 INTERNAL_SERVER_ERROR",
         "message":"500 Internal Server Error: [no body]",
         "identifier":null
      },
      {
         "endpoint":"getCas1CurrentPremises",
         "failureType":"UPSTREAM_HTTP_ERROR",
         "httpResponseStatus":"500 INTERNAL_SERVER_ERROR",
         "message":"500 Internal Server Error: [no body]",
         "identifier":null
      },
            {
         "endpoint":"getCas3CurrentPremises",
         "failureType":"UPSTREAM_HTTP_ERROR",
         "httpResponseStatus":"500 INTERNAL_SERVER_ERROR",
         "message":"500 Internal Server Error: [no body]",
         "identifier":null
      },
      {
         "endpoint":"getPrisoner",
         "failureType":"UPSTREAM_HTTP_ERROR",
         "httpResponseStatus":"500 INTERNAL_SERVER_ERROR",
         "message":"500 Internal Server Error: [no body]",
         "identifier":null
      }
   ]
}
""".trimIndent()

fun expectedGetAccommodationByIdResponse(
  crn: String,
  cprAddressId: UUID,
  createdAt: String,
): String = """
{
    "data": {
        "crn" : "$crn",
        "cprAddressId" : "$cprAddressId",
        "noFixedAbode": false,
        "typeVerified": true,
        "startDate": "$createdAt",
        "endDate": null,
        "address": {
            "buildingName": "test building name",
            "buildingNumber": "4",
            "country": "England",
            "county": "test county",
            "dependentLocality": "test dependent locality",
            "postcode": "test postcode",
            "postTown": "test post town",
            "subBuildingName": "test sub building name",
            "thoroughfareName": "test thoroughfare",
            "uprn": "test uprn"
        },
        "status": {
            "code": "PR",
            "description": "Proposed"
        },
        "type": {
            "code": "A07B",
            "description": "Living in the home of a friend, family member or partner: settled"
        }
    }
}
""".trimIndent()

fun expectedGetNextAccommodationWithUpstreamFailureResponse(): String = """
{
  "data":null,
  "upstreamFailures":[
  {
    "endpoint":"getCorePersonRecordByCrn",
    "failureType":"UPSTREAM_HTTP_ERROR",
    "httpResponseStatus":"500 INTERNAL_SERVER_ERROR",
    "message":"500 Internal Server Error: [no body]",
    "identifier":null
  }
  ]
}
""".trimIndent()
