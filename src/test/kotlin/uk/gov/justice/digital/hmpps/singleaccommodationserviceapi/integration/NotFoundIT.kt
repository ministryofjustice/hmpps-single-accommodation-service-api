package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class NotFoundIT : IntegrationTestBase() {

  @BeforeEach
  fun setup() {
    createTestDataSetupUserAndDeliusUser()
  }

  @Test
  fun `Resources that aren't found should return 404 - test of the exception handler`() {
    restTestClient.get().uri("/some-url-not-found")
      .withDeliusUserJwt()
      .exchange()
      .expectStatus().isNotFound
  }
}
