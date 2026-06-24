package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator

import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.web.client.RestClientException

class AggregatorServiceTest {
  private val upstreamFailureReporter = mockk<UpstreamFailureReporter>(relaxed = true)
  private val aggregatorService = AggregatorService(listOf(upstreamFailureReporter))

  @Test
  fun `reports handled async failures without failing the whole aggregation`() {
    val exception = RestClientException("upstream unavailable")

    val result = aggregatorService.orchestrateAsyncCalls(
      standardCallsNoIteration = mapOf(
        "workingCall" to { "success" },
        "failingCall" to { throw exception },
      ),
    )

    assertThat(result.standardCallsNoIterationResults?.getResult<String>("workingCall")).isEqualTo("success")
    assertThat(result.standardCallsNoIterationResults?.getFailures()).containsExactly(
      UpstreamFailure(
        callKey = "failingCall",
        type = FailureType.UNKNOWN_ERROR,
        errorDetail = ErrorDetail(message = "upstream unavailable"),
      ),
    )
    verify(exactly = 1) {
      upstreamFailureReporter.report(
        "failingCall",
        AggregatorCallOutcome.Failure(
          type = FailureType.UNKNOWN_ERROR,
          errorDetail = ErrorDetail(message = "upstream unavailable"),
        ),
        exception,
      )
    }
  }
}
