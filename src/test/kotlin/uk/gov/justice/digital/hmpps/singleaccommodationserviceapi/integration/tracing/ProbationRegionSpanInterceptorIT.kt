package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.tracing

import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.awaitility.kotlin.untilAsserted
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.config.TestOpenTelemetryConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildStaffDetail
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.tracing.USER_PROBATION_REGION_ATTRIBUTE
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.tracing.USER_PROBATION_REGION_HEADER
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.USERNAME_OF_LOGGED_IN_DELIUS_USER
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.HmppsAuthStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.ProbationIntegrationDeliusStubs

@Import(TestOpenTelemetryConfig::class)
class ProbationRegionSpanInterceptorIT : IntegrationTestBase() {

  @Autowired
  private lateinit var spanExporter: InMemorySpanExporter

  private val regionKey = AttributeKey.stringKey(USER_PROBATION_REGION_ATTRIBUTE)

  @BeforeEach
  fun setUpRequest() {
    spanExporter.reset()
    val deliusUser = createDeliusUser()
    HmppsAuthStubs.stubGrantToken()
    ProbationIntegrationDeliusStubs.stubGetStaffByUsername(
      deliusUsername = USERNAME_OF_LOGGED_IN_DELIUS_USER,
      response = buildStaffDetail(username = deliusUser.username, email = deliusUser.email!!),
    )
  }

  @Test
  fun `adds the probation region from the header to the current span`() {
    restTestClient.get().uri("/user/teams")
      .withDeliusUserJwt()
      .headers { it.set(USER_PROBATION_REGION_HEADER, "N07") }
      .exchangeSuccessfully()

    await.untilAsserted {
      val spans = spanExporter.finishedSpanItems
      assertThat(spans).hasSize(1)
      assertThat(spans.single().attributes.get(regionKey)).isEqualTo("N07")
    }
  }

  @Test
  fun `does not add the attribute when the header is absent`() {
    restTestClient.get().uri("/user/teams")
      .withDeliusUserJwt()
      .exchangeSuccessfully()

    await.untilAsserted {
      val spans = spanExporter.finishedSpanItems
      assertThat(spans).hasSize(1)
      assertThat(spans.single().attributes.get(regionKey)).isNull()
    }
  }
}
