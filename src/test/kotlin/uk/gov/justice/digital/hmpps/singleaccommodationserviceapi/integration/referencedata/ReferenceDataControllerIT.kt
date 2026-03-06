package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.referencedata

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ReferenceDataDto
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
      .expectBody(Array<ReferenceDataDto>::class.java)
      .value { results ->
        assertThat(results).isNotEmpty
        assertThat(results).hasSize(408)
        assertThat(results!!.first().name).isEqualTo("Aberdeen City")
        assertThat(results.last().name).isEqualTo("York")
      }
  }
}
