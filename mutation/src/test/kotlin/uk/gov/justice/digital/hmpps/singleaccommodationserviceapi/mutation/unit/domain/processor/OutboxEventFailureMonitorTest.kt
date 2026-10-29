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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.OutboxEventRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.sentry.SentryService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.OutboxEventFailureMonitor

@ExtendWith(MockKExtension::class)
class OutboxEventFailureMonitorTest {

  @MockK
  lateinit var outboxEventRepository: OutboxEventRepository

  @RelaxedMockK
  lateinit var sentryService: SentryService

  @InjectMockKs
  lateinit var outboxEventFailureMonitor: OutboxEventFailureMonitor

  @Test
  fun `does nothing when there are no failed outbox events`() {
    every { outboxEventRepository.countByProcessedStatus(ProcessedStatus.FAILED) } returns 0

    outboxEventFailureMonitor.reportFailedEvents()

    verify(exactly = 0) { sentryService.captureErrorMessage(any()) }
  }

  @Test
  fun `captures alert when failed outbox events exist`() {
    every { outboxEventRepository.countByProcessedStatus(ProcessedStatus.FAILED) } returns 5L

    outboxEventFailureMonitor.reportFailedEvents()

    verify {
      sentryService.captureErrorMessage(match { it.contains("5 failed outbox event(s)") })
    }
  }
}
