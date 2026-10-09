package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.unit.domain.processor

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseRefreshRequestStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRefreshRequestRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.sentry.SentryService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.CaseRefreshFailureMonitor

@ExtendWith(MockKExtension::class)
class CaseRefreshFailureMonitorTest {

  @MockK
  lateinit var caseRefreshRequestRepository: CaseRefreshRequestRepository

  @RelaxedMockK
  lateinit var sentryService: SentryService

  @InjectMockKs
  lateinit var caseRefreshFailureMonitor: CaseRefreshFailureMonitor

  @Test
  fun `does nothing when there are no failed case refresh requests`() {
    every { caseRefreshRequestRepository.countByStatus(CaseRefreshRequestStatus.FAILED) } returns 0

    caseRefreshFailureMonitor.reportFailedRequests()

    verify(exactly = 0) { sentryService.captureErrorMessage(any()) }
  }

  @Test
  fun `captures one alert when failed requests exist`() {
    every { caseRefreshRequestRepository.countByStatus(CaseRefreshRequestStatus.FAILED) } returns 2L

    caseRefreshFailureMonitor.reportFailedRequests()

    verify { sentryService.captureErrorMessage(match { it.contains("Detected 2 failed case refresh request(s)") }) }
  }

}
