package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.unit.domain.processor

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.InboxEventRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.InboxEventCleanupJob
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class InboxEventCleanupJobTest {
  private val inboxEventRepository = mockk<InboxEventRepository>()
  private val clock = Clock.fixed(Instant.parse("2026-10-15T05:00:00Z"), ZoneOffset.UTC)
  private val cleanup = InboxEventCleanupJob(inboxEventRepository, clock, 90)
  private val cutoff = Instant.parse("2026-07-17T05:00:00Z")

  @ParameterizedTest
  @ValueSource(ints = [0, 5])
  fun `deletes events created more than 90 days ago`(deletedCount: Int) {
    every { inboxEventRepository.deleteCreatedBefore(cutoff) } returns deletedCount

    cleanup.deleteOldEvents()

    verify(exactly = 1) { inboxEventRepository.deleteCreatedBefore(cutoff) }
  }

  @Test
  fun `does not suppress deletion failures`() {
    val failure = IllegalStateException("Database unavailable")
    every { inboxEventRepository.deleteCreatedBefore(cutoff) } throws failure

    assertThatThrownBy { cleanup.deleteOldEvents() }.isSameAs(failure)
  }

  @Test
  fun `uses configured retention period`() {
    val configuredCutoff = Instant.parse("2026-09-15T05:00:00Z")
    every { inboxEventRepository.deleteCreatedBefore(configuredCutoff) } returns 2

    InboxEventCleanupJob(inboxEventRepository, clock, 30).deleteOldEvents()

    verify(exactly = 1) { inboxEventRepository.deleteCreatedBefore(configuredCutoff) }
  }
}
