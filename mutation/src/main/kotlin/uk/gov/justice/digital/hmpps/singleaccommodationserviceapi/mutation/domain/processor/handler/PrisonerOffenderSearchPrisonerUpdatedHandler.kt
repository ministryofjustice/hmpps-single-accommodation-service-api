package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.handler

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.json.JsonMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.prisonersearch.PrisonerSearchClient
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.CategoriesChanged
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.IncomingHmppsDomainEventType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.PrisonerSearchDomainEvent
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.categoriesChanged
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.InboxEventEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProcessedStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.uri
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.InboxEventRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseApplicationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.InboxEventHandler
import java.time.Instant

@Component
class PrisonerOffenderSearchPrisonerUpdatedHandler(
  private val inboxEventRepository: InboxEventRepository,
  private val caseApplicationService: CaseApplicationService,
  private val jsonMapper: JsonMapper,
  private val prisonerSearchClient: PrisonerSearchClient,
) : InboxEventHandler {

  private val log = LoggerFactory.getLogger(javaClass)

  override fun supportedEventType() = IncomingHmppsDomainEventType.PRISONER_OFFENDER_SEARCH_PRISONER_UPDATED

  override fun getPartitionKey(inboxEvent: InboxEventEntity): String? {
    val prisonerSearchDomainEvent = jsonMapper.readValue(inboxEvent.payload, PrisonerSearchDomainEvent::class.java)
    return prisonerSearchDomainEvent.personReference.findNoms()
  }

  @Transactional
  override fun handle(inboxEvent: InboxEventEntity) {
    log.info("Processing Prisoner Offender Search Prisoner Updated event [inboxEventId={}]", inboxEvent.id)
    log.debug("Prisoner Search callback URL [detailUrl={}]", inboxEvent.eventDetailUrl)

    try {
      val prisonerSearchDomainEvent = jsonMapper.readValue(inboxEvent.payload, PrisonerSearchDomainEvent::class.java)

      val isRelevant = prisonerSearchDomainEvent.additionalInformation.categoriesChanged.contains(CategoriesChanged.SENTENCE)

      if (!isRelevant) {
        log.info("Sentence information has not changed so event is irrelevant")
        inboxEvent.processedStatus = ProcessedStatus.IGNORED
        inboxEvent.processedAt = Instant.now()
        inboxEventRepository.save(inboxEvent)
        return
      }

      val newPrisoner = prisonerSearchClient.getPrisoner(uri = inboxEvent.uri())
      log.info(
        "Prisoner fetched successfully [inboxEventId={}, releaseDate={}]",
        inboxEvent.id,
        newPrisoner.releaseDate,
      )
      log.debug("Prisoner response [inboxEventId={}, prisoner={}]", inboxEvent.id, newPrisoner)

      val nomsNumber = checkNotNull(prisonerSearchDomainEvent.personReference.findNoms()) {
        "NOMS number not found in event payload [inboxEventId=${inboxEvent.id}]"
      }

      // get crn
      val crn = "TEST"

      log.debug("Upserting case [inboxEventId={}, nomsNumber={}, crn={}]", inboxEvent.id, nomsNumber, crn)
      caseApplicationService.upsertReleaseDate(prisoner = newPrisoner, crn = crn)

      inboxEvent.processedStatus = ProcessedStatus.SUCCESS
      inboxEvent.processedAt = Instant.now()
      inboxEventRepository.save(inboxEvent)
      log.info("Prisoner Search event processed successfully [inboxEventId={}, nomsNumber={}, crn={}]", inboxEvent.id, nomsNumber, crn)
    } catch (e: Exception) {
      log.error(
        "Failed to process prisoner search event [inboxEventId={}, error={}]",
        inboxEvent.id,
        e.message,
      )
      log.debug("Prisoner Search processing failure details", e)
      inboxEvent.processedStatus = ProcessedStatus.FAILED
      inboxEvent.processedAt = Instant.now()
      inboxEventRepository.save(inboxEvent)
    }
  }
}
