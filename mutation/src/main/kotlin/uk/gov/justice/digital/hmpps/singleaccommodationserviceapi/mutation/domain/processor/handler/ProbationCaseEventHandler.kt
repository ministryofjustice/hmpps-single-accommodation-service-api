package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.handler

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.IncomingHmppsDomainEventType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseRefreshRequestService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.InboxEventHandler
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.InboxEventHelper

@Component
class ProbationCaseEventHandler(
  private val caseRepository: CaseRepository,
  private val caseRefreshRequestService: CaseRefreshRequestService?,
  private val inboxEventHelper: InboxEventHelper,
) : InboxEventHandler {

  private val log = LoggerFactory.getLogger(javaClass)

  override fun supportedEventTypes() = setOf(
    IncomingHmppsDomainEventType.PROBATION_CASE_REGISTRATION_ADDED.typeName,
    IncomingHmppsDomainEventType.PROBATION_CASE_REGISTRATION_DELETED.typeName,
    IncomingHmppsDomainEventType.PROBATION_CASE_REGISTRATION_DEREGISTERED.typeName,
    IncomingHmppsDomainEventType.PROBATION_CASE_REGISTRATION_UPDATED.typeName,
  )

  override fun getPartitionKey(inboxEvent: InboxEventHandler.InboxEvent): String = inboxEventHelper.findCrn(inboxEvent)

  override fun handle(inboxEvent: InboxEventHandler.InboxEvent): InboxEventHandler.Result {
    val crn = inboxEventHelper.findCrn(inboxEvent)
    return when (val case = caseRepository.findByCrn(crn)) {
      null -> InboxEventHandler.Result.IGNORED

      else -> {
        caseRefreshRequestService?.requestLiveRefresh(case.id)
        log.info(
          "PROBATION_CASE event processed successfully [inboxEventId={}, crn={}]",
          inboxEvent.id,
          crn,
        )
        InboxEventHandler.Result.PROCESSED
      }
    }
  }
}
