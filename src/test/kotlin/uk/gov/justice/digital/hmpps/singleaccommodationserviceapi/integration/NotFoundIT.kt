package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration

import org.awaitility.kotlin.await
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.HmppsAuthStubs
import java.time.Duration

class NotFoundIT : IntegrationTestBase() {

  @BeforeEach
  fun setup() {
    createDeliusUser()
    HmppsAuthStubs.stubGrantToken()
  }

  @Test
  fun `Resources that aren't found should return 404 - test of the exception handler`() {
    await
      .atMost(Duration.ofSeconds(10))
      .pollInterval(Duration.ofMillis(100))
      .logging()
      .untilAsserted {
        restTestClient.get().uri("/some-url-not-found")
          .exchange()
          .expectStatus().isEqualTo(404)
          .expectBody().jsonPath("$.userMessage")
          .isEqualTo("No resource found failure: No static resource some-url-not-found for request '/some-url-not-found'.")
      }
  }
}
