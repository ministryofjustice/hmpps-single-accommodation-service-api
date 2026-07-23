package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.unit.domain.processor

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildProposedAccommodationEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withCrn
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.IncomingHmppsDomainEventType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.PersonIdentifier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.PersonReference
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.SnsDomainEvent
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.ProposedAccommodationRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.AccommodationSyncService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseRefreshRequestService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.InboxEventHandler
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.InboxEventHelper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.handler.CprProbationAddressDeletedHandler
import java.time.OffsetDateTime
import java.util.UUID

@ExtendWith(MockKExtension::class)
class CprProbationAddressDeletedHandlerTest {
  @RelaxedMockK
  private lateinit var accommodationSyncService: AccommodationSyncService

  @MockK
  private lateinit var proposedAccommodationRepository: ProposedAccommodationRepository

  @MockK
  private lateinit var inboxEventHelper: InboxEventHelper

  @RelaxedMockK
  private lateinit var caseRefreshRequestService: CaseRefreshRequestService

  @MockK
  private lateinit var caseRepository: CaseRepository

  @InjectMockKs
  private lateinit var handler: CprProbationAddressDeletedHandler

  private val crn = UUID.randomUUID().toString()
  private val case = buildCaseEntity { withCrn(crn) }
  private val domainEvent =
    SnsDomainEvent(
      eventType = IncomingHmppsDomainEventType.CPR_PROBATION_ADDRESS_UPDATED.name,
      version = 1,
      description = "test event",
      detailUrl = "localhost",
      occurredAt = OffsetDateTime.now(),
      personReference =
      PersonReference(
        identifiers = listOf(PersonIdentifier("CRN", crn)),
      ),
      additionalInformation = mapOf("cprAddressId" to UUID.randomUUID().toString()),
    )
  private val inboxEvent = InboxEventHandler.InboxEvent(
    id = UUID.randomUUID(),
    eventDetailUrl = "localhost",
    payload = "payload",
  )

  @Test
  fun `should not refresh case and should ignore deleted message when case is not known`() {
    every { caseRepository.findByCrn(crn) } returns null
    every { inboxEventHelper.findCrn(inboxEvent) } returns crn
    assertThat(handler.handle(inboxEvent)).isEqualTo(InboxEventHandler.Result.IGNORED)
    verify(exactly = 0) { caseRefreshRequestService.requestLiveRefresh(any()) }
  }

  @Test
  fun `should refresh case but ignore deleted message when accommodation is not known`() {
    every { inboxEventHelper.toDomainEvent(inboxEvent) } returns domainEvent
    every { inboxEventHelper.findCrn(inboxEvent) } returns crn
    every { caseRepository.findByCrn(crn) } returns case
    every { proposedAccommodationRepository.findByCprAddressId(any()) } returns null
    assertThat(handler.handle(inboxEvent)).isEqualTo(InboxEventHandler.Result.IGNORED)
    verify(exactly = 1) { caseRefreshRequestService.requestLiveRefresh(any()) }
  }

  @Test
  fun `should refresh case and process deleted message when case and cprAddressId is known`() {
    val proposedAccommodationEntity = buildProposedAccommodationEntity()
    every { inboxEventHelper.toDomainEvent(inboxEvent) } returns domainEvent
    every { inboxEventHelper.findCrn(inboxEvent) } returns crn
    every { caseRepository.findByCrn(crn) } returns case
    every { proposedAccommodationRepository.findByCprAddressId(any()) } returns proposedAccommodationEntity
    assertThat(handler.handle(inboxEvent)).isEqualTo(InboxEventHandler.Result.PROCESSED)
    verify { caseRefreshRequestService.requestLiveRefresh(case.id) }
  }
}
