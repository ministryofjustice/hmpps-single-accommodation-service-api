package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.referencedata

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase

class ReferenceDataControllerIT : IntegrationTestBase() {

  @BeforeEach
  fun setup() {
    createTestDataSetupUserAndDeliusUser()
  }

  @Test
  fun `should return 200 with all local authority areas`() {
    restTestClient.get().uri("/reference-data?type=LOCAL_AUTHORITY_AREAS")
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectStatus().isOk
      .expectBody(String::class.java)
      .value { body ->
        val response = ObjectMapper().readTree(body)
        val results = response.get("data")
        assertThat(results).isNotNull
        assertThat(results.size()).isEqualTo(408)
        assertThat(results.first().get("name").asText()).isEqualTo("Aberdeen City")
        assertThat(results.last().get("name").asText()).isEqualTo("York")
      }
  }
}
