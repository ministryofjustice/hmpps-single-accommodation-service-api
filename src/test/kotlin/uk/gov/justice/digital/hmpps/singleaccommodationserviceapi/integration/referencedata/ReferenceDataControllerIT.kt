package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.referencedata

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ApiResponseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ReferenceDataDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase

class ReferenceDataControllerIT : IntegrationTestBase() {

  @BeforeEach
  fun setup() {
    createTestDataSetupUserAndDeliusUser()
  }

  @Test
  fun `should return 200 with all local authority areas`() {
    webTestClient.get().uri("/reference-data?type=LOCAL_AUTHORITY_AREAS")
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectStatus().isOk
      .expectBody<ApiResponseDto<List<ReferenceDataDto>>>()
      .value { body ->
        val results = body!!.data
        assertThat(results).isNotNull
        assertThat(results.size).isGreaterThan(407)
        assertThat(results.first().name).isEqualTo("Aberdeen City")
        assertThat(results.last().name).isEqualTo("York")
      }
  }

  @Test
  fun `should return 200 with type of accommodation_types`() {
    webTestClient.get().uri("/reference-data?type=ACCOMMODATION_TYPES")
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectStatus().isOk
      .expectBody<ApiResponseDto<List<ReferenceDataDto>>>()
      .value { body ->
        val results = body!!.data
        assertThat(results).isNotNull
        assertThat(results.size).isEqualTo(20)
        assertThat(results.first().code).isEqualTo("A02")
        assertThat(results.first().name).isEqualTo("Approved Premises")
        assertThat(results.last().code).isEqualTo("A03")
        assertThat(results.last().name).isEqualTo("Transient or short-term accommodation")
      }
  }

  @Test
  fun `should return 200 with type of proposed accommodation_types`() {
    webTestClient.get().uri("/reference-data?type=PROPOSED_ACCOMMODATION_TYPES")
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectStatus().isOk
      .expectBody<ApiResponseDto<List<ReferenceDataDto>>>()
      .value { body ->
        val results = body!!.data
        assertThat(results).isNotNull
        assertThat(results.size).isEqualTo(13)
        assertThat(results.first().code).isEqualTo("A10")
        assertThat(results.first().name).isEqualTo("CAS2 accommodation of 13 weeks or more")
        assertThat(results.last().code).isEqualTo("A03")
        assertThat(results.last().name).isEqualTo("Transient or short-term accommodation")
      }
  }

  @Test
  fun `should return 200 with type of accommodation_statuses`() {
    webTestClient.get().uri("/reference-data?type=ACCOMMODATION_STATUSES")
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectStatus().isOk
      .expectBody<ApiResponseDto<List<ReferenceDataDto>>>()
      .value { body ->
        val results = body!!.data
        assertThat(results).isNotNull
        assertThat(results.size).isEqualTo(9)
        assertThat(results.first().code).isEqualTo("B")
        assertThat(results.first().name).isEqualTo("Bail")
        assertThat(results.last().code).isEqualTo("S")
        assertThat(results.last().name).isEqualTo("Secondary")
      }
  }
}
