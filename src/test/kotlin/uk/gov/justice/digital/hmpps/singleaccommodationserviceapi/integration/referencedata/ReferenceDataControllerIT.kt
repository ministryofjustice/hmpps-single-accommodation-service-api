package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.referencedata

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.LocalAuthorityAreaDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase

class ReferenceDataControllerIT : IntegrationTestBase() {

  @Test
  fun `should return 200 with all active local authority areas`() {
    restTestClient.get().uri("/reference-data/local-authority-areas")
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectStatus().isOk
      .expectBody(Array<LocalAuthorityAreaDto>::class.java)
      .value { results ->
        assertThat(results).isNotEmpty
        assertThat(results!!.all { it.active }).isTrue()
      }
  }

  @Test
  fun `should filter results by search parameter`() {
    restTestClient.get().uri("/reference-data/local-authority-areas?search=City of London")
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectStatus().isOk
      .expectBody(Array<LocalAuthorityAreaDto>::class.java)
      .value { results ->
        assertThat(results).isNotEmpty
        assertThat(results!!.any { it.name == "City of London" }).isTrue()
      }
  }

  @Test
  fun `should return empty results for search with no matches`() {
    restTestClient.get().uri("/reference-data/local-authority-areas?search=XYZ")
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectStatus().isOk
      .expectBody(Array<LocalAuthorityAreaDto>::class.java)
      .value { results ->
        assertThat(results).isEmpty()
      }
  }
}
