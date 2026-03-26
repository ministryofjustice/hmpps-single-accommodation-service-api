package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.handler

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.json.JsonMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationdelius.ProbationIntegrationDeliusClient
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.IncomingHmppsDomainEventType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.SnsDomainEvent
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.InboxEventEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProcessedStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.InboxEventRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseApplicationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.InboxEventHandler
import java.time.Instant

@Component
class CaseAllocationHandler(
  private val inboxEventRepository: InboxEventRepository,
  private val caseApplicationService: CaseApplicationService,
  private val jsonMapper: JsonMapper,
  private val probationIntegrationDeliusClient: ProbationIntegrationDeliusClient,
  @Value($$"${case-list.onboarded-teams}") private val onboardedTeamsCodes: List<String>,
) : InboxEventHandler {

  private val log = LoggerFactory.getLogger(javaClass)

  override fun supportedEventType() = IncomingHmppsDomainEventType.CASE_ALLOCATED_TO_PROBATION_PRACTITIONER

  override fun getPartitionKey(inboxEvent: InboxEventEntity): String? {
    val caseAllocationEvent = jsonMapper.readValue(inboxEvent.payload, SnsDomainEvent::class.java)
    return caseAllocationEvent.personReference.findCrn()
  }

  @Transactional
  override fun handle(inboxEvent: InboxEventEntity) {
    log.info("Processing case-allocation event [inboxEventId={}]", inboxEvent.id)

    try {
      val crn = checkNotNull(getPartitionKey(inboxEvent)) {
        "CRN not found in event payload [inboxEventId=${inboxEvent.id}]"
      }
      val case = probationIntegrationDeliusClient.postCaseSummaries(crns = listOf(crn)).cases.first()
      if (onboardedTeamsCodes.contains(case.manager.team.code)) {
        caseApplicationService.createCase(case.crn)
        updateInboxEventStatusAndSave(inboxEvent, status = ProcessedStatus.SUCCESS)
      } else {
        updateInboxEventStatusAndSave(inboxEvent, status = ProcessedStatus.NOT_RELEVANT)
      }
      log.info("Tier event processed successfully [inboxEventId={}, crn={}]", inboxEvent.id, crn)
    } catch (e: Exception) {
      log.error(
        "Failed to process tier event [inboxEventId={}, error={}]",
        inboxEvent.id,
        e.message,
      )
      log.debug("Tier processing failure details", e)
      updateInboxEventStatusAndSave(inboxEvent, status = ProcessedStatus.FAILED)
    }
  }

  private fun updateInboxEventStatusAndSave(inboxEvent: InboxEventEntity, status: ProcessedStatus) {
    inboxEvent.processedStatus = status
    inboxEvent.processedAt = Instant.now()
    inboxEventRepository.save(inboxEvent)
  }
}
