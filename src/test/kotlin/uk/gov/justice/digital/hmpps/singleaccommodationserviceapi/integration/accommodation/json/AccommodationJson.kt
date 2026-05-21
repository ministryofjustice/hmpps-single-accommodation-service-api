package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.accommodation.json

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

fun expectedGetCurrentAccommodationResponse(): String = """
{
   "data":{
      "crn":"FAKECRN",
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

fun expectedGetCurrentAccommodationWithUpstreamFailureResponse(): String = """
{
   "data":null,
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
