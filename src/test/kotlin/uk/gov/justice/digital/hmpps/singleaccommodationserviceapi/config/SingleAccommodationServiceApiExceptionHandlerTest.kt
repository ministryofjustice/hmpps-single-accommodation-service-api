package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.config

import io.mockk.clearMocks
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.UpstreamFailureDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.UpstreamFailureType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.exception.UpstreamFailureException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.sentry.SentryService

class SingleAccommodationServiceApiExceptionHandlerTest {
  private val sentryService = mockk<SentryService>(relaxed = true)
  private val handler = SingleAccommodationServiceApiExceptionHandler(sentryService)

  @BeforeEach
  fun resetMocks() {
    clearMocks(sentryService)
  }

  @Test
  fun `captures unexpected exceptions in Sentry`() {
    val exception = RuntimeException("boom")

    val response = handler.handleException(exception)

    assertThat(response.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
    verify(exactly = 1) { sentryService.captureException(exception) }
  }

  @Test
  fun `captures upstream server failures in Sentry`() {
    val exception = upstreamFailureException(HttpStatus.BAD_GATEWAY)

    val response = handler.handleUpstreamFailureException(exception)

    assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_GATEWAY)
    verify(exactly = 1) { sentryService.captureException(exception) }
  }

  private fun upstreamFailureException(status: HttpStatus) = UpstreamFailureException(
    UpstreamFailureDto(
      endpoint = "test-endpoint",
      failureType = UpstreamFailureType.UPSTREAM_HTTP_ERROR,
      httpResponseStatus = status,
      message = "Upstream failed",
    ),
  )
}
