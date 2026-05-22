package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.sar

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.config.GrantType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.hmpps.kotlin.auth.AuthSource

class SarIntegrationTest : IntegrationTestBase() {

  private fun sarToken() = jwtAuthHelper.createJwtAccessToken(
    grantType = GrantType.CLIENT_CREDENTIALS.type,
    clientId = "sar-client",
    username = null,
    roles = listOf("SAR_DATA_ACCESS"),
    authSource = AuthSource.NONE.source,
  )

  private fun nonSarToken() = jwtAuthHelper.createJwtAccessToken(
    grantType = GrantType.CLIENT_CREDENTIALS.type,
    clientId = "other-client",
    username = null,
    roles = listOf("SOME_OTHER_ROLE"),
    authSource = AuthSource.NONE.source,
  )

  @Test
  fun `GET subject-access-request returns 400 when neither PRN nor CRN provided`() {
    restTestClient.get()
      .uri("/subject-access-request")
      .headers { it.setBearerAuth(sarToken()) }
      .exchange()
      .expectStatus().isBadRequest
  }

  @Test
  fun `GET subject-access-request returns 204 when no data found for CRN`() {
    restTestClient.get()
      .uri("/subject-access-request?crn=X000000")
      .headers { it.setBearerAuth(sarToken()) }
      .exchange()
      .expectStatus().isNoContent
  }

  @Test
  fun `GET subject-access-request returns 403 without SAR_DATA_ACCESS role`() {
    restTestClient.get()
      .uri("/subject-access-request?crn=X000000")
      .headers { it.setBearerAuth(nonSarToken()) }
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  fun `GET subject-access-request template returns 200 with mustache content`() {
    restTestClient.get()
      .uri("/subject-access-request/template")
      .headers { it.setBearerAuth(sarToken()) }
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_PLAIN)
      .expectBody(String::class.java)
      .value { body ->
        assertThat(body).contains("{{#Cases}}")
        assertThat(body).contains("{{#DutyToRefers}}")
        assertThat(body).contains("{{#ProposedAccommodations}}")
      }
  }

  @Test
  fun `GET subject-access-request template returns 403 without SAR_DATA_ACCESS role`() {
    restTestClient.get()
      .uri("/subject-access-request/template")
      .headers { it.setBearerAuth(nonSarToken()) }
      .exchange()
      .expectStatus().isForbidden
  }
}
