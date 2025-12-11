package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.eligibility.response

// fun expectedGetEligibilityResponse(crn: String): String = """
//  {
//    "crn": "$crn",
//    "cas1": {
//      "serviceStatus": "1",
//      "actions": ["Team 1"]
//    },
//  }
// """.trimIndent()

fun expectedGetEligibilityResponse(crn: String): String = """
  {
    "crn":"$crn",
    "cas1":{
      "serviceStatus":"NOT_ELIGIBLE",
      "actions":[]
    }
  }
""".trimIndent()
