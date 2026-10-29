package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.handler

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremisesanddelius.ApprovedPremisesAndDeliusClient
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.IncomingHmppsDomainEventType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseRefreshPriority
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.OnboardedTeamRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseApplicationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseRefreshRequestService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CrnToPrisonNumber
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.InboxEventHandler
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.InboxEventHelper

@Component
class PersonCommunityManagerAllocatedEventHandler(
  private val caseApplicationService: CaseApplicationService,
  private val inboxEventHelper: InboxEventHelper,
  private val approvedPremisesAndDeliusClient: ApprovedPremisesAndDeliusClient,
  private val onboardedTeamRepository: OnboardedTeamRepository,
  private val caseRepository: CaseRepository,
  private val caseRefreshRequestService: CaseRefreshRequestService,
) : InboxEventHandler {

  private val log = LoggerFactory.getLogger(javaClass)

  override fun supportedEventTypes() = setOf(IncomingHmppsDomainEventType.PERSON_COMMUNITY_MANAGER_ALLOCATED.typeName)

  override fun getPartitionKey(inboxEvent: InboxEventHandler.InboxEvent): String = inboxEventHelper.findCrn(inboxEvent)

  @Transactional
  override fun handle(inboxEvent: InboxEventHandler.InboxEvent): InboxEventHandler.Result {
    val crn = getPartitionKey(inboxEvent)

    caseRepository.findByCrn(crn)?.let {
      caseRefreshRequestService.requestLiveRefresh(it.id)
      return InboxEventHandler.Result.PROCESSED
    }

    val case = approvedPremisesAndDeliusClient.postCaseSummaries(crns = listOf(crn)).cases.single()
    return if (onboardedTeamRepository.existsByTeamCodeIsIgnoreCase(case.manager.team.code)) {
      log.info("Creating case from PERSON_COMMUNITY_MANAGER_ALLOCATED inbox event: [{}]", inboxEvent.id)
      caseApplicationService.createBlankCases(
        listOf(CrnToPrisonNumber(case.crn, case.nomsId)),
        refreshPriority = CaseRefreshPriority.LIVE,
      )
      InboxEventHandler.Result.PROCESSED
    } else {
      InboxEventHandler.Result.IGNORED
    }
  }
}
