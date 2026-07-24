package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.handler

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.json.JsonMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecordClient
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.IncomingHmppsDomainEventType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.SnsDomainEvent
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.ProposedAccommodationRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.AccommodationSyncService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.InboxEventHandler
import java.util.UUID

@Component
class CprProbationAddressUpdatedHandler(
  private val accommodationSyncService: AccommodationSyncService,
  private val proposedAccommodationRepository: ProposedAccommodationRepository,
  private val corePersonRecordClient: CorePersonRecordClient,
  private val jsonMapper: JsonMapper,
) : InboxEventHandler {

  private val log = LoggerFactory.getLogger(javaClass)

  override fun supportedEventType() = IncomingHmppsDomainEventType.CPR_PROBATION_ADDRESS_UPDATED.typeName

  override fun getPartitionKey(inboxEvent: InboxEventHandler.InboxEvent): String? {
    val cprProbationAddressUpdatedEvent = jsonMapper.readValue(inboxEvent.payload, SnsDomainEvent::class.java)
    val cprAddressId = cprProbationAddressUpdatedEvent.additionalInformation?.let { it["cprAddressId"] }
    return cprAddressId?.toString()
  }

  private fun getCrn(inboxEvent: InboxEventHandler.InboxEvent): String? {
    val caseAllocationEvent = jsonMapper.readValue(inboxEvent.payload, SnsDomainEvent::class.java)
    return caseAllocationEvent.personReference.findCrn()
  }

  @Transactional
  override fun handle(inboxEvent: InboxEventHandler.InboxEvent): InboxEventHandler.Result {
    log.info("Processing CPR_PROBATION_ADDRESS_UPDATED event [inboxEventId={}]", inboxEvent.id)
    val cprAddressIdString = checkNotNull(getPartitionKey(inboxEvent)) {
      "cprAddressId not found in event payload [inboxEventId=${inboxEvent.id}]"
    }
    val cprAddressId = UUID.fromString(cprAddressIdString)
    val relatedAccommodationEntity = proposedAccommodationRepository.findByCprAddressId(cprAddressId) ?: return InboxEventHandler.Result.IGNORED
    val crn = checkNotNull(getCrn(inboxEvent)) {
      "CRN not found in event payload [inboxEventId=${inboxEvent.id}]"
    }
    log.info("Found CRN in CPR_PROBATION_ADDRESS_UPDATED event [crn={}]", crn)
    log.info("Found case in SAS db that relates to updated address for CPR_PROBATION_ADDRESS_UPDATED event [cprAddressId={}]", cprAddressId)
    val probationAddress = corePersonRecordClient.getProbationAddress(uri = inboxEvent.uri())
    val result = accommodationSyncService.updateAccommodationRecordWithCprAddressUpdate(
      crn = crn,
      sasAccommodationRecord = relatedAccommodationEntity,
      cprAddressRecord = probationAddress,
    )
    return if (result) InboxEventHandler.Result.PROCESSED else InboxEventHandler.Result.FAILED
  }
}
