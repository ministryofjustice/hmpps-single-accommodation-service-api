package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.handler

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.IncomingHmppsDomainEventType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseRefreshRequestService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.InboxEventHandler
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.InboxEventHelper

@Component
class PrisonerOffenderSearchHandler(
  private val caseRepository: CaseRepository,
  private val caseRefreshRequestService: CaseRefreshRequestService?,
  private val inboxEventHelper: InboxEventHelper,
) : InboxEventHandler {

  private val log = LoggerFactory.getLogger(javaClass)

  override fun supportedEventTypes() = setOf(
    IncomingHmppsDomainEventType.PRISONER_OFFENDER_SEARCH_PRISONER_RECEIVED.typeName,
    IncomingHmppsDomainEventType.PRISONER_OFFENDER_SEARCH_PRISONER_UPDATED.typeName,
    IncomingHmppsDomainEventType.PRISONER_OFFENDER_SEARCH_PRISONER_RELEASED.typeName,
  )

  override fun getPartitionKey(inboxEvent: InboxEventHandler.InboxEvent): String = inboxEventHelper.findPrisonNumber(inboxEvent)

  override fun handle(inboxEvent: InboxEventHandler.InboxEvent): InboxEventHandler.Result {
    val prisonNumber = inboxEventHelper.findPrisonNumber(inboxEvent)
    return when (val case = caseRepository.findByPrisonNumber(prisonNumber)) {
      null -> InboxEventHandler.Result.IGNORED

      else -> {
        caseRefreshRequestService?.requestLiveRefresh(case.id)
        log.info(
          "PRISONER_OFFENDER_SEARCH event processed successfully [inboxEventId={}, prisonNumber={}]",
          inboxEvent.id,
          prisonNumber,
        )
        InboxEventHandler.Result.PROCESSED
      }
    }
  }
}
