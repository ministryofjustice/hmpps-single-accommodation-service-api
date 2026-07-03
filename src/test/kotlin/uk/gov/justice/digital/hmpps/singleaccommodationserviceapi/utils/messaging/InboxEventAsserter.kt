package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.messaging

import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import tools.jackson.databind.json.JsonMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.SnsDomainEvent
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProcessedStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.InboxEventRepository
import java.time.Duration.ofMillis
import java.time.Duration.ofSeconds

@Component
class InboxEventAsserter(private val inboxEventRepository: InboxEventRepository, private val jsonMapper: JsonMapper) {
  fun assertAllInboxMessagesProcessed(count: Int) {
    assertExpectedInboxEvents(ProcessedStatus.PROCESSED, count)
  }

  private fun assertExpectedInboxEvents(processedStatus: ProcessedStatus, count: Int) {
    await
      .atMost(ofSeconds(10))
      .pollInterval(ofMillis(100))
      .untilAsserted {
        assertThat(
          inboxEventRepository.findAllByProcessedStatus(processedStatus, Pageable.unpaged()),
        ).hasSize(count)
      }
  }

  fun assertInboxEvent(
    crn: String,
    eventType: String,
    eventDetailUrl: String?,
    processedStatus: ProcessedStatus,
  ) {
    await
      .atMost(ofSeconds(10))
      .pollInterval(ofMillis(100))
      .untilAsserted {
        val inboxEvents = inboxEventRepository.findAll()
        assertThat(inboxEvents).hasSize(1)
        val inboxEvent = inboxEvents.first()
        val caseAllocationEvent = jsonMapper.readValue(inboxEvent.payload, SnsDomainEvent::class.java)
        assertThat(caseAllocationEvent.personReference.findCrn()).isEqualTo(crn)
        assertThat(inboxEvent.eventType).isEqualTo(eventType)
        assertThat(inboxEvent.eventDetailUrl).isEqualTo(eventDetailUrl)
        assertThat(inboxEvent.processedStatus).isEqualTo(processedStatus)
      }
  }
}
