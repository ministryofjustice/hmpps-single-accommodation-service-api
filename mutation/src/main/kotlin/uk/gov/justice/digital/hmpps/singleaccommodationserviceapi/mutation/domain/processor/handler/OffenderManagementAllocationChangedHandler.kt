package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.handler

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecordClient
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.IncomingHmppsDomainEventType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.SnsDomainEvent
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.UserRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseCreationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseRefreshRequestService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.InboxEventHandler
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.InboxEventHelper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.getAdditionalInformation
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.getRequiredAdditionalInformation

@ConfigurationProperties(prefix = "case-list")
class OffenderManagementAllocationChangedProperties(
  var onboardedPrisonCodes: List<String> = emptyList(),
)

@Component
class OffenderManagementAllocationChangedHandler(
  private val caseCreationService: CaseCreationService,
  private val inboxEventHelper: InboxEventHelper,
  private val userRepository: UserRepository,
  private val caseRepository: CaseRepository,
  private val caseRefreshRequestService: CaseRefreshRequestService?,
  private val corePersonRecordClient: CorePersonRecordClient,
  private val offenderManagementAllocationChangedProperties: OffenderManagementAllocationChangedProperties,
) : InboxEventHandler {

  override fun supportedEventTypes() = setOf(IncomingHmppsDomainEventType.OFFENDER_MANAGEMENT_ALLOCATION_CHANGED.typeName)

  override fun getPartitionKey(inboxEvent: InboxEventHandler.InboxEvent): String = inboxEventHelper.findPrisonNumber(inboxEvent)

  /**
   * This handler processes offender-management.allocation.changed events, which are sent when a prisoner is allocated
   * to a NOMIS user. This service tracks offenders of interest via the sas_case table, which are created either when a
   * user views their caselist, or by being manually preloaded. This event will be processed:
   *  a) update - if the prisonNumber from the event is in the database, we will refresh the case.
   * -OR-
   *  b) create - if the prisonNumber is unknown but allocated user exists in the sas_users table, or if the team has
   *  been onboarded. we create a populated entry into the SAS_CASE table.
   * -OR-
   *  c) ignore - if neither are known, we ignore the event.
   */
  @Transactional
  override fun handle(inboxEvent: InboxEventHandler.InboxEvent): InboxEventHandler.Result {
    val prisonNumber = getPartitionKey(inboxEvent)
    val case = caseRepository.findByPrisonNumber(prisonNumber)
    if (case != null) {
      caseRefreshRequestService?.requestLiveRefresh(case.id)
      return InboxEventHandler.Result.PROCESSED
    }

    val event = inboxEventHelper.toDomainEvent(inboxEvent)
    if (prisonIsOnboarded(event) || staffMemberIsKnown(event)) {
      val cpr = corePersonRecordClient.getByPrisonNumber(prisonNumber)
      val identifiers = cpr.identifiers

      /**
       * This requires a single CRN to create the case, which may mean identifying which CRN is current and creating
       * the case using that. Any exceptions will be caught in the dispatcher, and the message failed and reported accordingly.
       */
      require(identifiers?.crns?.size == 1) { "This requires a single CRN in cpr identifiers for prisonNumber: [$prisonNumber]." }

      val crn = identifiers.crns.single()
      caseCreationService.upsertCase(crn, prisonNumber)
      return InboxEventHandler.Result.PROCESSED
    } else {
      return InboxEventHandler.Result.IGNORED
    }
  }

  private fun prisonIsOnboarded(event: SnsDomainEvent): Boolean = offenderManagementAllocationChangedProperties.onboardedPrisonCodes.contains(
    event.getRequiredAdditionalInformation(
      "prisonId",
    ),
  )

  private fun staffMemberIsKnown(event: SnsDomainEvent): Boolean {
    val staffCode = event.getAdditionalInformation("staffCode")
    return staffCode != null && userRepository.findByNomisStaffId(staffCode.toLong()) != null
  }
}
