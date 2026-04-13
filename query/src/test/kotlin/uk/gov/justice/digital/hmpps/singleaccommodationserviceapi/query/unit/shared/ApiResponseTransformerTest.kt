package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.shared

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.IdentifierType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.UpstreamFailureType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.ErrorDetail
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.FailureType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildUpstreamFailure
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.shared.ApiResponseTransformer
import java.util.UUID

class ApiResponseTransformerTest {

  @Nested
  inner class ToApiResponseDto {

    @Test
    fun `should map data and upstream failures correctly`() {
      val crn = UUID.randomUUID().toString()
      val data = "test data"
      val upstreamFailures = listOf(
        buildUpstreamFailure(
          callKey = "someCall",
          type = FailureType.UPSTREAM_HTTP_ERROR,
          errorDetail = ErrorDetail(
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR,
            message = "Upstream error",
          ),
          identifier = crn,
        ),
      )

      val result = ApiResponseTransformer.toApiResponseDto(data, upstreamFailures)

      assertThat(result.data).isEqualTo("test data")
      assertThat(result.upstreamFailures).hasSize(1)
      assertThat(result.upstreamFailures[0].endpoint).isEqualTo("someCall")
      assertThat(result.upstreamFailures[0].failureType).isEqualTo(UpstreamFailureType.UPSTREAM_HTTP_ERROR)
      assertThat(result.upstreamFailures[0].httpResponseStatus).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
      assertThat(result.upstreamFailures[0].message).isEqualTo("Upstream error")
      assertThat(result.upstreamFailures[0].identifier!!.type).isEqualTo(IdentifierType.CRN)
      assertThat(result.upstreamFailures[0].identifier!!.value).isEqualTo(crn)
    }

    @Test
    fun `should default to empty upstream failures`() {
      val data = "test data"

      val result = ApiResponseTransformer.toApiResponseDto(data)

      assertThat(result.data).isEqualTo("test data")
      assertThat(result.upstreamFailures).isEmpty()
    }

    @Test
    fun `should handle multiple upstream failures`() {
      val data = "test data"
      val upstreamFailures = listOf(
        buildUpstreamFailure(
          callKey = "someCall",
          type = FailureType.UPSTREAM_HTTP_ERROR,
          errorDetail = ErrorDetail(
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR,
            message = "Upstream error",
          ),
        ),
        buildUpstreamFailure(
          callKey = "someSlowCall",
          type = FailureType.TIMEOUT,
          errorDetail = ErrorDetail(
            message = "Request timed out",
          ),
        ),
      )

      val result = ApiResponseTransformer.toApiResponseDto(data, upstreamFailures)

      assertThat(result.upstreamFailures).hasSize(2)
      assertThat(result.upstreamFailures[0].endpoint).isEqualTo("someCall")
      assertThat(result.upstreamFailures[1].endpoint).isEqualTo("someSlowCall")
    }
  }
}
