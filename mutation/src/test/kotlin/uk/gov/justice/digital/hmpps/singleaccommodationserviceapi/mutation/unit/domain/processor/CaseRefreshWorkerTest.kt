package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.unit.domain.processor

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseRefreshFailureCategory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseRefreshPriority
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.UserContextService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.sentry.SentryService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseRefreshFailure
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseRefreshFailureClassifier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseRefreshProcessor
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseRefreshProperties
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseRefreshRequestService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.CaseRefreshWorker
import java.util.UUID

@ExtendWith(MockKExtension::class)
class CaseRefreshWorkerTest {

  @MockK
  lateinit var caseRefreshRequestService: CaseRefreshRequestService

  @MockK
  lateinit var caseRefreshProcessor: CaseRefreshProcessor

  @RelaxedMockK
  lateinit var userContextService: UserContextService

  @RelaxedMockK
  lateinit var sentryService: SentryService

  @MockK
  lateinit var failureClassifier: CaseRefreshFailureClassifier

  private val properties = CaseRefreshProperties()

  @InjectMockKs
  lateinit var caseRefreshWorker: CaseRefreshWorker

  @Test
  fun `returns zero stats when there are no pending claims`() {
    every { caseRefreshRequestService.claimPending(any(), any()) } returns emptyList()

    val stats = caseRefreshWorker.process()

    assertThat(stats.withoutDuration()).isEqualTo(CaseRefreshWorker.Stats())
    verify(exactly = 0) { caseRefreshProcessor.process(any()) }
    verify(exactly = 0) { userContextService.setUserContextAsSasSystemUser() }
    verify(exactly = 0) { userContextService.clearContext() }
  }

  @Test
  fun `counts refreshed and failed outcomes across claims`() {
    val refreshedClaim = claim()
    val failedClaim = claim()
    every { caseRefreshRequestService.claimPending(any(), any()) } returns listOf(refreshedClaim, failedClaim)
    every { caseRefreshProcessor.process(refreshedClaim) } returns CaseRefreshProcessor.Result.Refreshed
    every { caseRefreshProcessor.process(failedClaim) } returns CaseRefreshProcessor.Result.Failed

    val stats = caseRefreshWorker.process()

    assertThat(stats.withoutDuration()).isEqualTo(
      CaseRefreshWorker.Stats(claimedCount = 2, refreshedCount = 1, failedCount = 1),
    )
    verify(exactly = 2) { userContextService.setUserContextAsSasSystemUser() }
    verify(exactly = 2) { userContextService.clearContext() }
  }

  @Test
  fun `counts case not found and stale claim outcomes separately from failures`() {
    val caseNotFoundClaim = claim()
    val staleClaim = claim()
    every { caseRefreshRequestService.claimPending(any(), any()) } returns listOf(caseNotFoundClaim, staleClaim)
    every { caseRefreshProcessor.process(caseNotFoundClaim) } returns CaseRefreshProcessor.Result.CaseNotFound
    every { caseRefreshProcessor.process(staleClaim) } returns CaseRefreshProcessor.Result.IgnoredStaleClaim

    val stats = caseRefreshWorker.process()

    assertThat(stats.withoutDuration()).isEqualTo(
      CaseRefreshWorker.Stats(claimedCount = 2, caseNotFoundCount = 1, staleClaimCount = 1),
    )
    verify(exactly = 0) { caseRefreshRequestService.recordFailure(any(), any()) }
    verify(exactly = 0) { sentryService.captureException(any()) }
    verify(exactly = 2) { userContextService.clearContext() }
  }

  @Test
  fun `records an unexpected processor exception through the retry policy`() {
    val claim = claim()
    val exception = IllegalStateException("Unexpected failure")
    val failure = CaseRefreshFailure(
      category = CaseRefreshFailureCategory.UNEXPECTED_ERROR,
      detail = exception.message!!,
    )
    every { caseRefreshRequestService.claimPending(any(), any()) } returns listOf(claim)
    every { caseRefreshProcessor.process(claim) } throws exception
    every { failureClassifier.unexpected(exception) } returns failure
    every {
      caseRefreshRequestService.recordFailure(claim, failure)
    } returns CaseRefreshRequestService.FailureDisposition.HANDLED

    val stats = caseRefreshWorker.process()

    assertThat(stats.withoutDuration()).isEqualTo(
      CaseRefreshWorker.Stats(claimedCount = 1, failedCount = 1),
    )
    verify { caseRefreshRequestService.recordFailure(claim, failure) }
    verify { sentryService.captureException(exception) }
    verify { userContextService.clearContext() }
  }

  @Test
  fun `captures error when recording unexpected failure also fails`() {
    val claim = claim()
    val processingException = IllegalStateException("Unexpected failure")
    val failure = CaseRefreshFailure(
      category = CaseRefreshFailureCategory.UNEXPECTED_ERROR,
      detail = processingException.message!!,
    )
    val recordingException = RuntimeException("Database unavailable")

    every { caseRefreshRequestService.claimPending(any(), any()) } returns listOf(claim)
    every { caseRefreshProcessor.process(claim) } throws processingException
    every { failureClassifier.unexpected(processingException) } returns failure
    every { caseRefreshRequestService.recordFailure(claim, failure) } throws recordingException

    val stats = caseRefreshWorker.process()

    assertThat(stats.withoutDuration()).isEqualTo(
      CaseRefreshWorker.Stats(claimedCount = 1, failedCount = 1),
    )
    verify { sentryService.captureException(processingException) }
    verify { sentryService.captureException(recordingException) }
    verify { userContextService.clearContext() }
  }

  private fun claim(priority: CaseRefreshPriority = CaseRefreshPriority.BULK) = CaseRefreshRequestService.Claim(
    caseId = UUID.randomUUID(),
    generation = 1,
    claimId = UUID.randomUUID(),
    priority = priority,
  )

  private fun CaseRefreshWorker.Stats.withoutDuration() = copy(durationMillis = 0)
}
