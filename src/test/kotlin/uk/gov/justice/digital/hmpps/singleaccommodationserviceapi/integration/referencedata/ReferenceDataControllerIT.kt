package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.referencedata

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.JsonHelper.jsonMapper

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
        val response = jsonMapper.readTree(body)
        val results = response.get("data")
        assertThat(results).isNotNull
        assertThat(results.size()).isEqualTo(408)
        assertThat(results.first().get("name").asString()).isEqualTo("Aberdeen City")
        assertThat(results.last().get("name").asString()).isEqualTo("York")
      }
  }

  @Test
  fun `should return 200 with type of accommodation_types`() {
    restTestClient.get().uri("/reference-data?type=ACCOMMODATION_TYPES")
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectStatus().isOk
      .expectBody(String::class.java)
      .value { body ->
        val response = jsonMapper.readTree(body)
        val results = response.get("data")
        assertThat(results).isNotNull
        assertThat(results.size()).isEqualTo(13)
        assertThat(results.first().get("code").asString()).isEqualTo("A10")
        assertThat(results.first().get("name").asString()).isEqualTo("CAS2 accommodation of 13 weeks or more")
        assertThat(results.last().get("code").asString()).isEqualTo("A03")
        assertThat(results.last().get("name").asString()).isEqualTo("Transient or short-term accommodation")
      }
  }
}
