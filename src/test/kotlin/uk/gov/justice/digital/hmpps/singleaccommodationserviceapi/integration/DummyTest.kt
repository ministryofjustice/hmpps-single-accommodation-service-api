package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import org.junit.jupiter.api.Test

class DummyTest : IntegrationTestBase() {
  @Test
  fun `Get hello world`() {
    val jwt = jwtAuthHelper.createJwtAccessToken()
    webTestClient.get()
      .uri("/hello-world")
      .header("Authorization", "Bearer $jwt")
      .exchange()
      .expectStatus()
      .isOk
  }

  @Test
  fun `Get premises`() {
    val username = "TEST"
    mockClientCredentialsJwtRequest(username, listOf("ROLE_PROBATION"), authSource = "delius")


    val jwt = jwtAuthHelper.createJwtAccessToken()

    wiremockServer.stubFor(
      get(WireMock.urlEqualTo("/premises/summary"))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody("""
              [{"id":"c05c3839-402a-406d-abeb-790fb7ae5bd3","name":"the premises name 3","apArea":{"id":"58fd6185-e083-4bf8-929b-45b3b51c78e0","name":"the ap area name 2","code":null},"bedCount":0,"supportsSpaceBookings":true,"fullAddress":"LYHIIFRRAQ","postcode":"CR9 XB7","apCode":"ICPJWNHMRV"}]
            """.trimIndent())
            .withStatus(200),
        ),
    )
    webTestClient.get()
      .uri("/premises/summary")
      .header("Authorization", "Bearer $jwt")
      .exchange()
      .expectStatus()
      .isOk
  }
}
