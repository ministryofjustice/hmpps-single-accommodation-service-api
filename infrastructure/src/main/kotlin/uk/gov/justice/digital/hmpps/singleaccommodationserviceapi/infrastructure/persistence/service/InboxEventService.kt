package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.service

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.InboxEventEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProcessedStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.InboxEventRepository
import java.time.Instant

@Service
class InboxEventService(
  private val inboxEventRepository: InboxEventRepository,
) {
  fun findPendingOldestFirst(maxSize: Int): List<InboxEventEntity> {
    val pageable = PageRequest.of(
      0,
      maxSize,
      Sort.by("eventOccurredAt").ascending(),
    )

    return inboxEventRepository.findAllByProcessedStatus(ProcessedStatus.PENDING, pageable)
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  fun saveInboxEvent(inboxEvent: InboxEventEntity) {
    inboxEventRepository.save(inboxEvent)
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  fun updateInboxEventStatusAndSave(inboxEvent: InboxEventEntity, status: ProcessedStatus) {
    inboxEvent.processedStatus = status
    inboxEvent.processedAt = Instant.now()
    inboxEventRepository.save(inboxEvent)
  }
}
