package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.handler

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecordClient
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.IncomingHmppsDomainEventType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.UserRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseApplicationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseRefreshRequestService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.InboxEventHandler
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.InboxEventHelper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.getRequiredAdditionalInformation

@Component
class OffenderManagementAllocationChangedHandler(
  private val caseApplicationService: CaseApplicationService,
  private val inboxEventHelper: InboxEventHelper,
  private val userRepository: UserRepository,
  private val caseRepository: CaseRepository,
  private val caseRefreshRequestService: CaseRefreshRequestService?,
  private val corePersonRecordClient: CorePersonRecordClient,
) : InboxEventHandler {

  override fun supportedEventTypes() = setOf(IncomingHmppsDomainEventType.OFFENDER_MANAGEMENT_ALLOCATION_CHANGED.typeName)

  override fun getPartitionKey(inboxEvent: InboxEventHandler.InboxEvent): String = inboxEventHelper.findPrisonNumber(inboxEvent)

  @Transactional
  override fun handle(inboxEvent: InboxEventHandler.InboxEvent): InboxEventHandler.Result {
    val prisonNumber = getPartitionKey(inboxEvent)
    val case = caseRepository.findByPrisonNumber(prisonNumber)
    if (case != null) {
      caseRefreshRequestService?.requestLiveRefresh(case.id)
      return InboxEventHandler.Result.PROCESSED
    }

    val event = inboxEventHelper.toDomainEvent(inboxEvent)
    val staffCode = event.getRequiredAdditionalInformation("staffCode").toLong()

    val user = userRepository.findByNomisStaffId(staffCode)
    if (user != null) {
      val cpr = corePersonRecordClient.getByPrisonNumber(prisonNumber)
      val identifiers = cpr.identifiers

      // This will require a single CRN to create the case, which may mean identifying which CRN is current and creating
      // the case using that. Any exceptions will be caught in the dispatcher, and the message failed and reported accordingly.
      require(identifiers?.crns?.size == 1) { "This requires a single CRN in cpr identifiers for prisonNumber: [$prisonNumber]." }

      val crn = identifiers.crns.single()
      caseApplicationService.upsertCase(crn, prisonNumber)
      return InboxEventHandler.Result.PROCESSED
    }
    return InboxEventHandler.Result.IGNORED
  }
}
