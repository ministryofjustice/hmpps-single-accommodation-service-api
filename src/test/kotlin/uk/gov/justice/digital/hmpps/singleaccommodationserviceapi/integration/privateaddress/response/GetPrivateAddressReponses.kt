package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.privateaddress.response

fun expectedGetPrivateAddressesResponse(crn: String): String = """
{
   "crn":"$crn",
   "addresses":[
      {
         "id":"p5e22e29-36bf-48e5-bfc9-915176298cb0",
         "status":"checked",
         "address":{
            "line1":"Flat 7",
            "line2":"20 Main Road",
            "region":"Oxford",
            "city":"Oxford",
            "postcode":"OX2 6ZZ"
         },
         "addedBy":{
            "id":"user-1237",
            "name":"Danny Smith",
            "role":"Probation Officer"
         },
         "addedDate":"1970-01-01T00:00:00"
      },
      {
         "id":"l5e22e29-36bf-48e5-bfc9-915176298cb0",
         "status":"unsuitable",
         "address":{
            "line1":"Flat 9",
            "line2":"70 Main Road",
            "region":"Oxford",
            "city":"Oxford",
            "postcode":"OX2 6ZZ"
         },
         "addedBy":{
            "id":"user-1234",
            "name":"Peter Smith",
            "role":"Probation Officer"
         },
         "addedDate":"1970-01-01T00:00:00"
      }
   ]
}
""".trimIndent()
