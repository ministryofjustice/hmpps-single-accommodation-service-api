package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.handler

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.json.JsonMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecordClient
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.canonical.CanonicalAddress
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.IncomingHmppsDomainEventType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.SnsDomainEvent
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.ProposedAccommodationRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.AccommodationSyncService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.InboxEventHandler
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.utils.isProposedAccommodationStatus
import java.util.UUID

@Component
class CprProbationAddressCreatedHandler(
  private val accommodationSyncService: AccommodationSyncService,
  private val proposedAccommodationRepository: ProposedAccommodationRepository,
  private val caseRepository: CaseRepository,
  private val corePersonRecordClient: CorePersonRecordClient,
  private val jsonMapper: JsonMapper,
) : InboxEventHandler {

  private val log = LoggerFactory.getLogger(javaClass)

  override fun supportedEventTypes() = setOf(IncomingHmppsDomainEventType.CPR_PROBATION_ADDRESS_CREATED.typeName)

  override fun getPartitionKey(inboxEvent: InboxEventHandler.InboxEvent): String? {
    val cprProbationAddressCreatedEvent = jsonMapper.readValue(inboxEvent.payload, SnsDomainEvent::class.java)
    val cprAddressId = cprProbationAddressCreatedEvent.additionalInformation?.let { it["cprAddressId"] }
    return cprAddressId?.toString()
  }

  private fun getCrn(inboxEvent: InboxEventHandler.InboxEvent): String? {
    val event = jsonMapper.readValue(inboxEvent.payload, SnsDomainEvent::class.java)
    return event.personReference.findCrn()
  }

  private fun isProposedAccommodationAddress(probationAddress: CanonicalAddress) = probationAddress.status.code.isProposedAccommodationStatus()

  @Transactional
  override fun handle(inboxEvent: InboxEventHandler.InboxEvent): InboxEventHandler.Result {
    log.info("Processing CPR_PROBATION_ADDRESS_CREATED event [inboxEventId={}]", inboxEvent.id)
    val cprAddressIdString = checkNotNull(getPartitionKey(inboxEvent)) {
      "cprAddressId not found in event payload [inboxEventId=${inboxEvent.id}]"
    }
    val cprAddressId = UUID.fromString(cprAddressIdString)
    val crn = checkNotNull(getCrn(inboxEvent)) {
      "CRN not found in event payload [inboxEventId=${inboxEvent.id}]"
    }
    log.info("Found CRN in CPR_PROBATION_ADDRESS_CREATED event [crn={}]", crn)
    val caseEntity = caseRepository.findByCrn(crn) ?: return InboxEventHandler.Result.IGNORED
    val probationAddress = corePersonRecordClient.getProbationAddress(uri = inboxEvent.uri())
    if (!isProposedAccommodationAddress(probationAddress)) {
      return InboxEventHandler.Result.IGNORED
    }
    log.info("Found case in SAS db that relates to created proposed accommodation address for CPR_PROBATION_ADDRESS_CREATED event [cprAddressId={}]", cprAddressId)
    val existingAccommodationEntity = proposedAccommodationRepository.findByCprAddressId(cprAddressId)
    val result = if (existingAccommodationEntity != null) {
      log.info("Found existing accommodation record matching CPR_PROBATION_ADDRESS_CREATED event, updating for idempotency [cprAddressId={}]", cprAddressId)
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
    return if (result) InboxEventHandler.Result.PROCESSED else InboxEventHandler.Result.FAILED
  }
}
