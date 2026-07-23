package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.handler

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremisesanddelius.ApprovedPremisesAndDeliusClient
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.IncomingHmppsDomainEventType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseApplicationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.InboxEventHandler
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.InboxEventHelper

@Component
class CaseAllocationHandler(
  private val caseApplicationService: CaseApplicationService,
  private val inboxEventHelper: InboxEventHelper,
  private val approvedPremisesAndDeliusClient: ApprovedPremisesAndDeliusClient,
  @field:Value($$"${case-list.onboarded-teams}") private val onboardedTeamsCodes: List<String>,
) : InboxEventHandler {

  private val log = LoggerFactory.getLogger(javaClass)

  override fun supportedEventTypes() = setOf(IncomingHmppsDomainEventType.CASE_ALLOCATED.typeName)

  override fun getPartitionKey(inboxEvent: InboxEventHandler.InboxEvent): String = inboxEventHelper.findCrn(inboxEvent)

  @Transactional
  override fun handle(inboxEvent: InboxEventHandler.InboxEvent): InboxEventHandler.Result {
    log.info("Processing CaseAllocation event [inboxEventId={}]", inboxEvent.id)

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
      InboxEventHandler.Result.IGNORED
    }
  }
}
