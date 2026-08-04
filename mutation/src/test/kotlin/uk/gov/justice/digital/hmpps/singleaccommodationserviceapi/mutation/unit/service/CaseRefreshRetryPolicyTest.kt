package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.unit.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.ErrorDetail
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.FailureType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.UpstreamFailure
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseRefreshFailureCategory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseRefreshFailure
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseRefreshFailureClassifier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseRefreshProperties
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseRefreshRetryDecision
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseRefreshRetryPolicy
import java.time.Duration
import java.time.Instant

class CaseRefreshRetryPolicyTest {
  private val now = Instant.parse("2026-07-23T10:00:00Z")
  private val properties = CaseRefreshProperties(
    maxAttempts = 4,
    initialBackoff = Duration.ofMinutes(1),
    maxBackoff = Duration.ofMinutes(3),
  )
  private val policy = CaseRefreshRetryPolicy(properties)
  private val classifier = CaseRefreshFailureClassifier()

  @Test
  fun `transient failures use capped exponential backoff`() {
    val failure = CaseRefreshFailure(
      CaseRefreshFailureCategory.UPSTREAM_SERVER_ERROR,
      "Tier returned 500",
    )

    assertThat(policy.decide(failure, previousAttempts = 0, failedAt = now))
      .isEqualTo(CaseRefreshRetryDecision.RetryAt(now.plus(Duration.ofMinutes(1))))
    assertThat(policy.decide(failure, previousAttempts = 1, failedAt = now))
      .isEqualTo(CaseRefreshRetryDecision.RetryAt(now.plus(Duration.ofMinutes(2))))
    assertThat(policy.decide(failure, previousAttempts = 2, failedAt = now))
      .isEqualTo(CaseRefreshRetryDecision.RetryAt(now.plus(Duration.ofMinutes(3))))
  }

  @Test
  fun `retryable failure becomes terminal when attempts are exhausted`() {
    val failure = CaseRefreshFailure(
      CaseRefreshFailureCategory.UPSTREAM_TIMEOUT,
      "Timed out",
    )

    assertThat(policy.decide(failure, previousAttempts = 3, failedAt = now))
      .isEqualTo(CaseRefreshRetryDecision.FailPermanently)
  }

  @Test
  fun `permanent failure is not retried`() {
    val failure = CaseRefreshFailure(
      CaseRefreshFailureCategory.UPSTREAM_CLIENT_ERROR,
      "Bad request",
    )

    assertThat(policy.decide(failure, previousAttempts = 0, failedAt = now))
      .isEqualTo(CaseRefreshRetryDecision.FailPermanently)
  }

  @Test
  fun `classifier maps timeout server and client failures`() {
    val timeout = classifier.classify(
      listOf(upstreamFailure(FailureType.TIMEOUT)),
    )
    val serverError = classifier.classify(
      listOf(upstreamFailure(FailureType.UPSTREAM_HTTP_ERROR, HttpStatus.BAD_GATEWAY)),
    )
    val clientError = classifier.classify(
      listOf(upstreamFailure(FailureType.UPSTREAM_HTTP_ERROR, HttpStatus.BAD_REQUEST)),
    )

    assertThat(timeout.category).isEqualTo(CaseRefreshFailureCategory.UPSTREAM_TIMEOUT)
    assertThat(serverError.category).isEqualTo(CaseRefreshFailureCategory.UPSTREAM_SERVER_ERROR)
    assertThat(clientError.category).isEqualTo(CaseRefreshFailureCategory.UPSTREAM_CLIENT_ERROR)
  }

  @Test
  fun `classifier treats missing current Tier as permanent`() {
    val failure = classifier.classify(
      listOf(
        upstreamFailure(
          type = FailureType.UPSTREAM_HTTP_ERROR,
          status = HttpStatus.NOT_FOUND,
          callKey = ApiCallKeys.GET_TIER,
        ),
      ),
    )

    assertThat(failure.category).isEqualTo(CaseRefreshFailureCategory.CURRENT_TIER_NOT_FOUND)
  }

  private fun upstreamFailure(
    type: FailureType,
    status: HttpStatus? = null,
    callKey: String = ApiCallKeys.GET_TIER,
  ) = UpstreamFailure(
    callKey = callKey,
    type = type,
    errorDetail = ErrorDetail(status, "Upstream failed"),
  )
}
