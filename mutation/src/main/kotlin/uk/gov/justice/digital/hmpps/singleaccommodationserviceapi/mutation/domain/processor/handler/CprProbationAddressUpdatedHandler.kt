package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.handler

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecordClient
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.IncomingHmppsDomainEventType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.ProposedAccommodationRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.AccommodationSyncService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseRefreshRequestService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.InboxEventHandler
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.InboxEventHelper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.getAddtionalInformation
import java.util.UUID

@Component
class CprProbationAddressUpdatedHandler(
  private val accommodationSyncService: AccommodationSyncService,
  private val proposedAccommodationRepository: ProposedAccommodationRepository,
  private val corePersonRecordClient: CorePersonRecordClient,
  private val inboxEventHelper: InboxEventHelper,
  private val caseRepository: CaseRepository,
  private val caseRefreshRequestService: CaseRefreshRequestService?,
) : InboxEventHandler {

  private val log = LoggerFactory.getLogger(javaClass)
  private val eventType = IncomingHmppsDomainEventType.CPR_PROBATION_ADDRESS_UPDATED.typeName

  override fun supportedEventTypes() = setOf(eventType)

  override fun getPartitionKey(inboxEvent: InboxEventHandler.InboxEvent): String {
    val cprProbationAddressCreatedEvent = inboxEventHelper.toDomainEvent((inboxEvent))
    val cprAddressId = cprProbationAddressCreatedEvent.getAddtionalInformation("cprAddressId")
    return cprAddressId
  }

  override fun handle(inboxEvent: InboxEventHandler.InboxEvent): InboxEventHandler.Result {
    log.info("Processing CPR_PROBATION_ADDRESS_UPDATED event [inboxEventId={}]", inboxEvent.id)
    val crn = inboxEventHelper.findCrn(inboxEvent)
    val caseEntity = caseRepository.findByCrn(crn) ?: return InboxEventHandler.Result.IGNORED

    // this triggers a refresh regardless of whether we successfully process this message.
    caseRefreshRequestService?.requestLiveRefresh(caseEntity.id)

    val cprAddressIdString = checkNotNull(getPartitionKey(inboxEvent))
    val cprAddressId = UUID.fromString(cprAddressIdString)
    val relatedAccommodationEntity = proposedAccommodationRepository.findWithNotesByCprAddressId(cprAddressId) ?: return InboxEventHandler.Result.IGNORED

    log.info("Found CRN in {} event [crn={}]", eventType, crn)
    log.info(
      "Found case in SAS db that relates to updated address for {} event [cprAddressId={}]",
      eventType,
      cprAddressId,
    )
    val probationAddress = corePersonRecordClient.getProbationAddress(uri = inboxEvent.uri())
    val result = accommodationSyncService.updateAccommodationRecordWithCprAddressUpdate(
      crn = crn,
      sasAccommodationRecord = relatedAccommodationEntity,
      cprAddressRecord = probationAddress,
    )
    return if (result) {
      InboxEventHandler.Result.PROCESSED
    } else {
      InboxEventHandler.Result.FAILED
    }
  }
}
