package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.handler

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.IncomingHmppsDomainEventType.CPR_PROBATION_ADDRESS_DELETED
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.ProposedAccommodationRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.AccommodationSyncService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseRefreshRequestService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.InboxEventHandler
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.InboxEventHelper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.getRequiredAdditionalInformation
import java.util.UUID

@Component
class CprProbationAddressDeletedHandler(
  private val accommodationSyncService: AccommodationSyncService,
  private val proposedAccommodationRepository: ProposedAccommodationRepository,
  private val inboxEventHelper: InboxEventHelper,
  private val caseRefreshRequestService: CaseRefreshRequestService?,
  private val caseRepository: CaseRepository,
) : InboxEventHandler {

  private val log = LoggerFactory.getLogger(javaClass)
  private val eventType = CPR_PROBATION_ADDRESS_DELETED.typeName

  override fun supportedEventTypes() = setOf(eventType)

  override fun getPartitionKey(inboxEvent: InboxEventHandler.InboxEvent): String = inboxEventHelper.toDomainEvent((inboxEvent)).getRequiredAdditionalInformation("cprAddressId")

  override fun handle(inboxEvent: InboxEventHandler.InboxEvent): InboxEventHandler.Result {
    val crn = inboxEventHelper.findCrn(inboxEvent)
    caseRepository.findByCrn(crn)?.let {
      // this triggers a refresh regardless of whether we successfully process this message.
      caseRefreshRequestService?.requestLiveRefresh(it.id)
    }

    val cprAddressIdString = getPartitionKey(inboxEvent)
    val cprAddressId = UUID.fromString(cprAddressIdString)
    val accommodationToDelete =
      proposedAccommodationRepository.findByCprAddressId(cprAddressId) ?: return InboxEventHandler.Result.IGNORED

    log.info("Found accommodation match for {} event [cprAddressId={}]", eventType, cprAddressId)
    accommodationSyncService.softDeleteAccommodationRecordNoLongerInCpr(accommodationToDelete)

    return InboxEventHandler.Result.PROCESSED
  }
}
