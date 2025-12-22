package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.dutytorefer.response

fun expectedGetDutyToRefersResponse(crn: String): String = """
  [{
    "id":"b5e22e29-36bf-48e5-bfc9-915176298cb0",
    "crn":"$crn",
    "submittedTo":"Mock District Council A!!",
    "reference":"mock-abcd!!",
    "submitted":"1970-01-01T00:00:00",
    "status":"submitted!!",
    "outcome":"pending!!"
  }]
""".trimIndent()
