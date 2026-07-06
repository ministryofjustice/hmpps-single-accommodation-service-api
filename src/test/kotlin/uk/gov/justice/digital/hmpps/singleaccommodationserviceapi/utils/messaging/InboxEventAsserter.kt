package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.messaging

import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProcessedStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.InboxEventRepository
import java.time.Duration.ofMillis
import java.time.Duration.ofSeconds

@Component
class InboxEventAsserter(private val inboxEventRepository: InboxEventRepository) {
  fun assertAllInboxMessagesProcessed(count: Int) {
    await
      .atMost(ofSeconds(10))
      .pollInterval(ofMillis(100))
      .logging()
      .untilAsserted {
        assertThat(
          inboxEventRepository.findAllByProcessedStatus(ProcessedStatus.PROCESSED, Pageable.unpaged()),
        ).hasSize(count)
      }
  }
}
