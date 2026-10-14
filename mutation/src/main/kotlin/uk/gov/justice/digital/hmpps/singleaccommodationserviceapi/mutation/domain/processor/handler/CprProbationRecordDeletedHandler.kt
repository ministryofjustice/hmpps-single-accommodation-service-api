package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.handler

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.IncomingHmppsDomainEventType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRefreshRequestRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.InboxEventHandler
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.InboxEventHelper

@Component
class CprProbationRecordDeletedHandler(
  private val caseRepository: CaseRepository,
  private val inboxEventHelper: InboxEventHelper,
  private val caseRefreshRequestRepository: CaseRefreshRequestRepository,
) : InboxEventHandler {

  private val eventType = IncomingHmppsDomainEventType.CPR_PROBATION_RECORD_DELETED.typeName

  override fun supportedEventTypes() = setOf(eventType)

  override fun getPartitionKey(inboxEvent: InboxEventHandler.InboxEvent): String = inboxEventHelper.findCrn(inboxEvent)

  /**
   * Archival is a single atomic, version-incrementing update, so it can't be lost to (or lose) a concurrent
   * refresh. The pending refresh request is removed in the same transaction.
   */
  @Transactional
  override fun handle(inboxEvent: InboxEventHandler.InboxEvent): InboxEventHandler.Result {
    val crn = inboxEventHelper.findCrn(inboxEvent)
    val caseEntity = caseRepository.findByCrn(crn) ?: return InboxEventHandler.Result.IGNORED

    caseRefreshRequestRepository.deleteById(caseEntity.id)
    return when (caseRepository.archiveById(caseEntity.id)) {
      0 -> InboxEventHandler.Result.IGNORED
      else -> InboxEventHandler.Result.PROCESSED
    }
  }
}
