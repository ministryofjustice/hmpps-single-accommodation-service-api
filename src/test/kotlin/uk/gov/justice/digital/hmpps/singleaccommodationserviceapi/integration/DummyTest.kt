package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.model.Cas1PremisesBasicSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.model.NamedId
import java.util.UUID

class DummyTest : IntegrationTestBase() {
  @Test
  fun `Get hello world`() {
    webTestClient.get()
      .uri("/hello-world")
      .headers(setAuthorisation())
      .exchange()
      .expectStatus()
      .isOk
  }

  @Test
  fun `Get premises`() {
    mockClientCredentialsJwtRequest(
      "TEST",
      listOf("ROLE_APPROVED_PREMISES__SINGLE_ACCOMMODATION_SERVICE_API__RO"),
      authSource = "delius",
    )

    wiremockServer.stubFor(
      get(WireMock.urlEqualTo("/premises/summary"))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(
              """
              [{"id":"c05c3839-402a-406d-abeb-790fb7ae5bd3","name":"the premises name 3","apArea":{"id":"58fd6185-e083-4bf8-929b-45b3b51c78e0","name":"the ap area name 2","code":null},"bedCount":0,"supportsSpaceBookings":true,"fullAddress":"LYHIIFRRAQ","postcode":"CR9 XB7","apCode":"ICPJWNHMRV"}]
              """.trimIndent(),
            )
            .withStatus(200),
        ),
    )
    val result = webTestClient.get()
      .uri("/premises/summary")
      .headers(setAuthorisation())
      .exchange()
      .expectStatus()
      .isOk
      .expectBodyList(Cas1PremisesBasicSummary::class.java)
      .returnResult()
      .responseBody

    assertThat(result).isEqualTo(
      listOf(
        Cas1PremisesBasicSummary(
          id = UUID.fromString("c05c3839-402a-406d-abeb-790fb7ae5bd3"),
          name = "the premises name 3",
          apArea = NamedId(
            id = UUID.fromString("58fd6185-e083-4bf8-929b-45b3b51c78e0"),
            name = "the ap area name 2",
            code = null,
          ),
          bedCount = 0,
          supportsSpaceBookings = true,
          fullAddress = "LYHIIFRRAQ",
          postcode = "CR9 XB7",
          apCode = "ICPJWNHMRV",
        ),
      ),
    )
  }
}
