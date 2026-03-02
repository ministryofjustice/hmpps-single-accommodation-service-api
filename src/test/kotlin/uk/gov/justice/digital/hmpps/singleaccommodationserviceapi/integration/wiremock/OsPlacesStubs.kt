package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock

import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.WireMockInitializer.Companion.sasWiremock

object OsPlacesStubs {

  fun stubPostcodeSearch(postcode: String, responseBody: String) {
    sasWiremock.stubFor(
      get(urlPathEqualTo("/os-places/postcode"))
        .willReturn(okJson(responseBody)),
    )
  }

  fun osPlacesResponse(
    postcode: String,
    totalResults: Int = 1,
    offset: Int = 0,
    maxResults: Int = 50,
  ) = """
    {
      "header": {
        "totalresults": $totalResults,
        "maxresults": $maxResults,
        "offset": $offset
      },
      "results": [
        {
          "LPI": {
            "UPRN": "10014006768",
            "ADDRESS": "HEALTH CENTRE, HEALTH CENTRE ROAD, UNIVERSITY OF WARWICK, COVENTRY, $postcode",
            "PAO_TEXT": "HEALTH CENTRE",
            "STREET_DESCRIPTION": "HEALTH CENTRE ROAD",
            "LOCALITY_NAME": "UNIVERSITY OF WARWICK",
            "TOWN_NAME": "COVENTRY",
            "ADMINISTRATIVE_AREA": "COVENTRY",
            "POSTCODE_LOCATOR": "$postcode",
            "CLASSIFICATION_CODE": "CM02",
            "CLASSIFICATION_CODE_DESCRIPTION": "General Practice Surgery / Clinic",
            "STATUS": "APPROVED",
            "X_COORDINATE": 430193.0,
            "Y_COORDINATE": 275760.0,
            "MATCH": 1.0,
            "MATCH_DESCRIPTION": "EXACT"
          }
        }
      ]
    }
  """.trimIndent()

  fun osPlacesEmptyResponse(offset: Int = 0, maxResults: Int = 50) = """
    {
      "header": {
        "totalresults": 0,
        "maxresults": $maxResults,
        "offset": $offset
      },
      "results": []
    }
  """.trimIndent()
}
