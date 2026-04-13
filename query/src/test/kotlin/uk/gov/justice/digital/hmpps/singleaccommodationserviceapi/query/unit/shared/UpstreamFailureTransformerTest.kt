package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.shared

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.IdentifierType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.UpstreamFailureType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.ErrorDetail
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.FailureType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.UpstreamFailure
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildUpstreamFailure
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.shared.UpstreamFailureTransformer
import java.util.UUID

class UpstreamFailureTransformerTest {

  @Nested
  inner class ToUpstreamFailureDto {

    @Test
    fun `should map all fields correctly`() {
      val crn = UUID.randomUUID().toString()
      val failure = buildUpstreamFailure(
        callKey = "someCall",
        type = FailureType.UPSTREAM_HTTP_ERROR,
        errorDetail = ErrorDetail(
          httpStatus = HttpStatus.INTERNAL_SERVER_ERROR,
          message = "Upstream error",
        ),
        identifier = crn,
      )

      val result = UpstreamFailureTransformer.toUpstreamFailureDto(failure)

      assertThat(result.endpoint).isEqualTo("someCall")
      assertThat(result.failureType).isEqualTo(UpstreamFailureType.UPSTREAM_HTTP_ERROR)
      assertThat(result.httpResponseStatus).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
      assertThat(result.message).isEqualTo("Upstream error")
      assertThat(result.identifier).isNotNull()
      assertThat(result.identifier!!.type).isEqualTo(IdentifierType.CRN)
      assertThat(result.identifier!!.value).isEqualTo(crn)
    }

    @Test
    fun `should handle null identifier`() {
      val failure = buildUpstreamFailure(
        callKey = "someCall",
        type = FailureType.TIMEOUT,
        errorDetail = ErrorDetail(
          message = "Request timed out",
        ),
      )

      val result = UpstreamFailureTransformer.toUpstreamFailureDto(failure)

      assertThat(result.identifier).isNull()
    }

    @Test
    fun `should handle null httpStatus`() {
      val failure = buildUpstreamFailure(
        callKey = "someCall",
        type = FailureType.UNKNOWN_ERROR,
        errorDetail = ErrorDetail(
          message = "Upstream error",
        ),
      )

      val result = UpstreamFailureTransformer.toUpstreamFailureDto(failure)

      assertThat(result.httpResponseStatus).isNull()
    }
  }

  @Nested
  inner class ToFailureIdentifier {

    @Test
    fun `should map all fields correctly`() {
      val crn = UUID.randomUUID().toString()
      val result = UpstreamFailureTransformer.toFailureIdentifier(crn)

      assertThat(result.type).isEqualTo(IdentifierType.CRN)
      assertThat(result.value).isEqualTo(crn)
    }
  }

  @Nested
  inner class EnumMappings {

    @ParameterizedTest
    @EnumSource(FailureType::class)
    fun `should map all FailureType values correctly`(failureType: FailureType) {
      val failure = UpstreamFailure(
        callKey = "test",
        type = failureType,
        errorDetail = ErrorDetail(message = "test"),
      )

      val result = UpstreamFailureTransformer.toUpstreamFailureDto(failure)

      assertThat(result.failureType.name).isEqualTo(failureType.name)
    }
  }
}
