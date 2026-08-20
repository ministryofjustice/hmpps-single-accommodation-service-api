package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.handler

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.IncomingHmppsDomainEventType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseRefreshRequestService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.InboxEventHandler
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.InboxEventHelper

@Component
class CprProbationRecordUpdatedHandler(
  private val caseRepository: CaseRepository,
  private val inboxEventHelper: InboxEventHelper,
  private val caseRefreshRequestService: CaseRefreshRequestService?,
) : InboxEventHandler {

  private val eventType = IncomingHmppsDomainEventType.CPR_PROBATION_ADDRESS_CREATED.typeName

  override fun supportedEventTypes() = setOf(eventType)

  override fun getPartitionKey(inboxEvent: InboxEventHandler.InboxEvent): String = inboxEventHelper.findCrn(inboxEvent)

  override fun handle(inboxEvent: InboxEventHandler.InboxEvent): InboxEventHandler.Result {
    val crn = inboxEventHelper.findCrn(inboxEvent)
    return when (val caseEntity = caseRepository.findByCrn(crn)) {
      null -> InboxEventHandler.Result.IGNORED

      else -> {
        caseRefreshRequestService?.requestLiveRefresh(caseEntity.id)
        InboxEventHandler.Result.PROCESSED
      }
    }
  }
}
