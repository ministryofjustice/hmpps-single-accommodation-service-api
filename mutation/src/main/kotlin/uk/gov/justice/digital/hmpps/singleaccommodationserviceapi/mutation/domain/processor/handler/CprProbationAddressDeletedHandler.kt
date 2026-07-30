package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.handler

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.json.JsonMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.IncomingHmppsDomainEventType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.SnsDomainEvent
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.ProposedAccommodationRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.AccommodationSyncService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.InboxEventHandler
import java.util.UUID

@Component
class CprProbationAddressDeletedHandler(
  private val accommodationSyncService: AccommodationSyncService,
  private val proposedAccommodationRepository: ProposedAccommodationRepository,
  private val jsonMapper: JsonMapper,
) : InboxEventHandler {

  private val log = LoggerFactory.getLogger(javaClass)

  override fun supportedEventType() = IncomingHmppsDomainEventType.CPR_PROBATION_ADDRESS_DELETED.typeName

  override fun getPartitionKey(inboxEvent: InboxEventHandler.InboxEvent): String? {
    val cprProbationAddressDeletedEvent = jsonMapper.readValue(inboxEvent.payload, SnsDomainEvent::class.java)
    val cprAddressId = cprProbationAddressDeletedEvent.additionalInformation?.let { it["cprAddressId"] }
    return cprAddressId?.toString()
  }

  @Transactional
  override fun handle(inboxEvent: InboxEventHandler.InboxEvent): InboxEventHandler.Result {
    log.info("Processing CPR_PROBATION_ADDRESS_DELETED event [inboxEventId={}]", inboxEvent.id)
    val cprAddressIdString = checkNotNull(getPartitionKey(inboxEvent)) {
      "cprAddressId not found in event payload [inboxEventId=${inboxEvent.id}]"
    }
    val cprAddressId = UUID.fromString(cprAddressIdString)
    val accommodationToDelete = proposedAccommodationRepository.findByCprAddressId(cprAddressId)
    if (accommodationToDelete != null) {
      log.info("Found accommodation match for CPR_PROBATION_ADDRESS_DELETED event [cprAddressId={}]", cprAddressId)
      accommodationSyncService.softDeleteAccommodationRecordNoLongerInCpr(
        accommodationToDelete,
      )
      return InboxEventHandler.Result.PROCESSED
    }
    return InboxEventHandler.Result.IGNORED
  }
}
