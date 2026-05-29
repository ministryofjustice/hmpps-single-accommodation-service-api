package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.addresslookup

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.assertions.assertThatJson
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.HmppsAuthStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.OsPlacesStubs

class AddressLookupControllerIT : IntegrationTestBase() {

  @BeforeEach
  fun setup() {
    HmppsAuthStubs.stubGrantToken()
    createTestDataSetupUserAndDeliusUser()
  }

  @Test
  fun `should return 200 with addresses for a valid postcode`() {
    val postcode = "CV4 7AL"
    OsPlacesStubs.stubPostcodeSearch(postcode, OsPlacesStubs.osPlacesResponse(postcode))

    restTestClient.get().uri("/address-lookup/postcode?postcode={postcode}", postcode)
      .withDeliusUserJwt()
      .exchange()
      .expectStatus().isOk
      .expectBody(String::class.java)
      .value {
        assertThatJson(it!!).matchesExpectedJson(
          """
          {
            "addresses": [
              {
                "uprn": "10014006768",
                "singleLine": "HEALTH CENTRE, HEALTH CENTRE ROAD, UNIVERSITY OF WARWICK, COVENTRY, CV4 7AL",
                "subBuildingName": null,
                "buildingName": "HEALTH CENTRE",
                "buildingNumber": null,
                "street": "HEALTH CENTRE ROAD",
                "locality": "UNIVERSITY OF WARWICK",
                "town": "COVENTRY",
                "county": "COVENTRY",
                "country": null,
                "postcode": "CV4 7AL",
                "classification": "General Practice Surgery / Clinic",
                "status": "APPROVED",
                "x": 430193.0,
                "y": 275760.0,
                "match": 1.0,
                "matchDescription": "EXACT"
              }
            ],
            "total": 1,
            "offset": 0,
            "maxResults": 50
          }
          """.trimIndent(),
        )
      }
  }

  @Test
  fun `should return 200 with empty addresses list when OS Places returns no results`() {
    val postcode = "SW1A 1AA"
    OsPlacesStubs.stubPostcodeSearch(postcode, OsPlacesStubs.osPlacesEmptyResponse())

    restTestClient.get().uri("/address-lookup/postcode?postcode={postcode}", postcode)
      .withDeliusUserJwt()
      .exchange()
      .expectStatus().isOk
      .expectBody(String::class.java)
      .value {
        assertThatJson(it!!).matchesExpectedJson(
          """
          {
            "addresses": [],
            "total": 0,
            "offset": 0,
            "maxResults": 50
          }
          """.trimIndent(),
        )
      }
  }

  @Test
  fun `should normalise postcode before calling OS Places`() {
    val postcode = "cv47al"
    OsPlacesStubs.stubPostcodeSearch("CV4 7AL", OsPlacesStubs.osPlacesResponse("CV4 7AL"))

    restTestClient.get().uri("/address-lookup/postcode?postcode={postcode}", postcode)
      .withDeliusUserJwt()
      .exchange()
      .expectStatus().isOk
  }

  @Test
  fun `should return 400 for an invalid postcode`() {
    restTestClient.get().uri("/address-lookup/postcode?postcode=INVALID")
      .withDeliusUserJwt()
      .exchange()
      .expectStatus().isBadRequest
  }

  @Test
  fun `should return 400 when maxResults is zero`() {
    restTestClient.get().uri("/address-lookup/postcode?postcode=CV4+7AL&maxResults=0")
      .withDeliusUserJwt()
      .exchange()
      .expectStatus().isBadRequest
  }

  @Test
  fun `should return 400 when maxResults exceeds 100`() {
    restTestClient.get().uri("/address-lookup/postcode?postcode=CV4+7AL&maxResults=101")
      .withDeliusUserJwt()
      .exchange()
      .expectStatus().isBadRequest
  }

  @Test
  fun `should return 400 when offset is negative`() {
    restTestClient.get().uri("/address-lookup/postcode?postcode=CV4+7AL&offset=-1")
      .withDeliusUserJwt()
      .exchange()
      .expectStatus().isBadRequest
  }

  @Test
  fun `should return 5xx when no JWT is provided`() {
    restTestClient.get().uri("/address-lookup/postcode?postcode=CV4+7AL")
      .exchange()
      .expectStatus().is5xxServerError
  }

  @Test
  fun `should return 403 when JWT does not have ROLE_PROBATION`() {
    restTestClient.get().uri("/address-lookup/postcode?postcode=CV4+7AL")
      .withDeliusUserJwt(roles = listOf("ROLE_OTHER"))
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  fun `should pass maxResults and offset query params through`() {
    val postcode = "CV4 7AL"
    OsPlacesStubs.stubPostcodeSearch(postcode, OsPlacesStubs.osPlacesResponse(postcode, totalResults = 200, offset = 10, maxResults = 25))

    restTestClient.get().uri("/address-lookup/postcode?postcode={postcode}&maxResults=25&offset=10", postcode)
      .withDeliusUserJwt()
      .exchange()
      .expectStatus().isOk
      .expectBody(String::class.java)
      .value {
        assertThatJson(it!!).matchesExpectedJson(
          """
          {
            "addresses": [
              {
                "uprn": "10014006768",
                "singleLine": "HEALTH CENTRE, HEALTH CENTRE ROAD, UNIVERSITY OF WARWICK, COVENTRY, CV4 7AL",
                "subBuildingName": null,
                "buildingName": "HEALTH CENTRE",
                "buildingNumber": null,
                "street": "HEALTH CENTRE ROAD",
                "locality": "UNIVERSITY OF WARWICK",
                "town": "COVENTRY",
                "county": "COVENTRY",
                "country": null,
                "postcode": "CV4 7AL",
                "classification": "General Practice Surgery / Clinic",
                "status": "APPROVED",
                "x": 430193.0,
                "y": 275760.0,
                "match": 1.0,
                "matchDescription": "EXACT"
              }
            ],
            "total": 200,
            "offset": 10,
            "maxResults": 25
          }
          """.trimIndent(),
        )
      }
  }
}
