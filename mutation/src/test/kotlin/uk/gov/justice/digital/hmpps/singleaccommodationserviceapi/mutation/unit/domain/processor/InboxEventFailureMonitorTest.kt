package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.unit.domain.processor

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProcessedStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.InboxEventRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.sentry.SentryService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.InboxEventFailureMonitor

@ExtendWith(MockKExtension::class)
class InboxEventFailureMonitorTest {

  @MockK
  lateinit var inboxEventRepository: InboxEventRepository

  @RelaxedMockK
  lateinit var sentryService: SentryService

  @InjectMockKs
  lateinit var inboxEventFailureMonitor: InboxEventFailureMonitor

  @Test
  fun `does nothing when there are no failed inbox events`() {
    every { inboxEventRepository.countByProcessedStatus(ProcessedStatus.FAILED) } returns 0

    inboxEventFailureMonitor.reportFailedEvents()

    verify(exactly = 0) { sentryService.captureErrorMessage(any()) }
  }

  @Test
  fun `captures alert when failed inbox events exist`() {
    every { inboxEventRepository.countByProcessedStatus(ProcessedStatus.FAILED) } returns 3L

    inboxEventFailureMonitor.reportFailedEvents()

    verify {
      sentryService.captureErrorMessage(match { it.contains("3 failed inbox event(s)") })
    }
  }
}
