package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.unit.domain.processor

import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseRefreshFailureCategory
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

  @RelaxedMockK
  lateinit var caseRefreshRequestService: CaseRefreshRequestService

  @RelaxedMockK
  lateinit var caseRefreshProcessor: CaseRefreshProcessor

  @RelaxedMockK
  lateinit var userContextService: UserContextService

  @RelaxedMockK
  lateinit var sentryService: SentryService

  @RelaxedMockK
  lateinit var failureClassifier: CaseRefreshFailureClassifier

  @Test
  fun `records an unexpected processor exception through the retry policy`() {
    val claim = CaseRefreshRequestService.Claim(
      caseId = UUID.randomUUID(),
      generation = 1,
      claimId = UUID.randomUUID(),
    )
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
    } returns CaseRefreshRequestService.FailureDisposition.Handled

    val stats = worker().process()

    assertThat(stats).isEqualTo(CaseRefreshWorker.Stats(refreshedCount = 0, failedCount = 1))
    verify { caseRefreshRequestService.recordFailure(claim, failure) }
    verify { sentryService.captureException(exception) }
    verify { userContextService.clearContext() }
  }

  private fun worker() = CaseRefreshWorker(
    caseRefreshRequestService = caseRefreshRequestService,
    caseRefreshProcessor = caseRefreshProcessor,
    userContextService = userContextService,
    properties = CaseRefreshProperties(),
    sentryService = sentryService,
    failureClassifier = failureClassifier,
  )
}
