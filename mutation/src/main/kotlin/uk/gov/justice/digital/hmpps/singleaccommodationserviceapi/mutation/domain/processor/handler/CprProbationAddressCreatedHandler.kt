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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.utils.isProposedAccommodationStatus
import java.util.UUID

@Component
class CprProbationAddressCreatedHandler(
  private val accommodationSyncService: AccommodationSyncService,
  private val proposedAccommodationRepository: ProposedAccommodationRepository,
  private val caseRepository: CaseRepository,
  private val corePersonRecordClient: CorePersonRecordClient,
  private val inboxEventHelper: InboxEventHelper,
  private val caseRefreshRequestService: CaseRefreshRequestService?,
) : InboxEventHandler {

  private val log = LoggerFactory.getLogger(javaClass)
  private val eventType = IncomingHmppsDomainEventType.CPR_PROBATION_ADDRESS_CREATED.typeName

  override fun supportedEventTypes() = setOf(eventType)

  override fun getPartitionKey(inboxEvent: InboxEventHandler.InboxEvent): String {
    val cprProbationAddressCreatedEvent = inboxEventHelper.toDomainEvent((inboxEvent))
    val cprAddressId = cprProbationAddressCreatedEvent.getAddtionalInformation("cprAddressId")
    return cprAddressId
  }

  override fun handle(inboxEvent: InboxEventHandler.InboxEvent): InboxEventHandler.Result {
    log.info("Processing {} event [inboxEventId={}]", eventType, inboxEvent.id)
    val crn = inboxEventHelper.findCrn(inboxEvent)
    val caseEntity = caseRepository.findByCrn(crn) ?: return InboxEventHandler.Result.IGNORED
    // this triggers a refresh regardless of whether processing the message fails later.
    caseRefreshRequestService?.requestLiveRefresh(caseEntity.id)

    val cprAddressIdString = getPartitionKey(inboxEvent)
    val cprAddressId = UUID.fromString(cprAddressIdString)

    val probationAddress = corePersonRecordClient.getProbationAddress(uri = inboxEvent.uri())
    if (!probationAddress.status.code.isProposedAccommodationStatus()) {
      return InboxEventHandler.Result.IGNORED
    }

    log.info("Processing proposedAccommodation [cprAddressId={}] for case [caseId={}]", cprAddressId, caseEntity.id)

    val existingAccommodationEntity = proposedAccommodationRepository.findWithNotesByCprAddressId(cprAddressId)
    val result = if (existingAccommodationEntity != null) {
      log.info(
        "Accommodation with [cprAddressId={}] already exists on {} event, updating for idempotency.",
        cprAddressId,
        eventType,
      )
      accommodationSyncService.updateAccommodationRecordWithCprAddressUpdate(
        crn = crn,
        sasAccommodationRecord = existingAccommodationEntity,
        cprAddressRecord = probationAddress,
      )
    } else {
      accommodationSyncService.createAccommodationRecordWithCprAddressCreate(
        crn = crn,
        case = caseEntity,
        cprAddressRecord = probationAddress,
      )
    }
    return if (result) {
      InboxEventHandler.Result.PROCESSED
    } else {
      InboxEventHandler.Result.FAILED
    }
  }
}
