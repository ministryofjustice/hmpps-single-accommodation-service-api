package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.model.CasesResponse

class CasesTest : IntegrationTestBase() {

  @Test
  fun `GET cases returns list of cases with mock data`() {
    val response = webTestClient.get()
      .uri("/cases")
      .headers(setAuthorisation())
      .exchange()
      .expectStatus().isOk
      .expectBody(CasesResponse::class.java)
      .returnResult()
      .responseBody!!

    assertThat(response.cases).isNotEmpty
    assertThat(response.cases).allMatch { it.crn.isNotBlank() && it.name.isNotBlank() }
  }

  @Test
  fun `GET cases returns 401 when no auth token provided`() {
    webTestClient.get()
      .uri("/cases")
      .exchange()
      .expectStatus().isUnauthorized
  }
}
