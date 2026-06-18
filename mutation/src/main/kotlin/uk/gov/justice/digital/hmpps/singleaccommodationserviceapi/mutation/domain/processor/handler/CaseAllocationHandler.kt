package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.handler

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.json.JsonMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremisesanddelius.ApprovedPremisesAndDeliusClient
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.IncomingHmppsDomainEventType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.SnsDomainEvent
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.InboxEventEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseApplicationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.InboxEventHandler

@Component
class CaseAllocationHandler(
  private val caseApplicationService: CaseApplicationService,
  private val jsonMapper: JsonMapper,
  private val approvedPremisesAndDeliusClient: ApprovedPremisesAndDeliusClient,
  @field:Value($$"${case-list.onboarded-teams}") private val onboardedTeamsCodes: List<String>,
) : InboxEventHandler {

  private val log = LoggerFactory.getLogger(javaClass)

  override fun supportedEventType() = IncomingHmppsDomainEventType.CASE_ALLOCATED

  override fun getPartitionKey(inboxEvent: InboxEventEntity): String? {
    val caseAllocationEvent = jsonMapper.readValue(inboxEvent.payload, SnsDomainEvent::class.java)
    return caseAllocationEvent.personReference.findCrn()
  }

  @Transactional
  override fun handle(inboxEvent: InboxEventEntity): InboxEventHandler.Result {
    log.info("Processing CaseAllocation event [inboxEventId={}]", inboxEvent.id)

    try {
      val crn = checkNotNull(getPartitionKey(inboxEvent)) {
        "CRN not found in event payload [inboxEventId=${inboxEvent.id}]"
      }
      val case = approvedPremisesAndDeliusClient.postCaseSummaries(crns = listOf(crn)).cases.first()
      val shouldProcess = onboardedTeamsCodes.contains(case.manager.team.code)
      if (shouldProcess) {
        caseApplicationService.upsertCase(case.crn, case.nomsId)
      }
      log.info("CaseAllocation event processed successfully [inboxEventId={}, crn={}]", inboxEvent.id, crn)

      return if (shouldProcess) {
        InboxEventHandler.Result.PROCESSED
      } else {
        InboxEventHandler.Result.NOT_PROCESSED
      }
    } catch (e: Exception) {
      log.error(
        "Failed to process CaseAllocation event [inboxEventId={}, error={}]",
        inboxEvent.id,
        e.message,
      )
      log.debug("CaseAllocation processing failure details", e)
      return InboxEventHandler.Result.FAILED
    }
  }
}
