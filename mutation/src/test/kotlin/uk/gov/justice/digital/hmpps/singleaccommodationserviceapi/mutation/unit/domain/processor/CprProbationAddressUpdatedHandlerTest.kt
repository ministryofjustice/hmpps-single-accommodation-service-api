package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.unit.domain.processor

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecordClient
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.canonical.CanonicalAddress
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.canonical.CanonicalAddressStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCanonicalAddress
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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.handler.CprProbationAddressUpdatedHandler
import java.net.URI
import java.time.OffsetDateTime
import java.util.UUID

@ExtendWith(MockKExtension::class)
class CprProbationAddressUpdatedHandlerTest {
  @MockK
  private lateinit var accommodationSyncService: AccommodationSyncService

  @MockK
  private lateinit var proposedAccommodationRepository: ProposedAccommodationRepository

  @MockK
  private lateinit var corePersonRecordClient: CorePersonRecordClient

  @MockK
  private lateinit var inboxEventHelper: InboxEventHelper

  @RelaxedMockK
  private lateinit var caseRefreshRequestService: CaseRefreshRequestService

  @MockK
  private lateinit var caseRepository: CaseRepository

  @InjectMockKs
  private lateinit var handler: CprProbationAddressUpdatedHandler

  private val crn = UUID.randomUUID().toString()
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
  private val caseEntity = buildCaseEntity { withCrn(crn) }

  @BeforeEach
  fun setUp() {
    every { inboxEventHelper.toDomainEvent(inboxEvent) } returns domainEvent
    every { inboxEventHelper.findCrn(inboxEvent) } returns crn
  }

  @Test
  fun `should refresh case but ignore updated message when accommodation is not known`() {
    every { proposedAccommodationRepository.findWithNotesByCprAddressId(any()) } returns null
    every { caseRepository.findByCrn(crn) } returns caseEntity
    assertThat(handler.handle(inboxEvent)).isEqualTo(InboxEventHandler.Result.IGNORED)
    verify(exactly = 1) { caseRefreshRequestService.requestLiveRefresh(caseEntity.id) }
  }

  @Test
  fun `should not refresh case and should update when case is not known but accommodation is`() {
    every { caseRepository.findByCrn(crn) } returns null
    every { proposedAccommodationRepository.findByCprAddressId(any()) } returns null
    every { proposedAccommodationRepository.findWithNotesByCprAddressId(any()) } returns buildProposedAccommodationEntity()
    every { corePersonRecordClient.getProbationAddress(URI.create(domainEvent.detailUrl!!)) } returns buildCanonicalAddress()
    every {
      accommodationSyncService.updateAccommodationRecordWithCprAddressUpdate(
        any(),
        any(),
        any(),
      )
    } returns true
    assertThat(handler.handle(inboxEvent)).isEqualTo(InboxEventHandler.Result.PROCESSED)
    verify(exactly = 0) { caseRefreshRequestService.requestLiveRefresh(any()) }
  }

  @ParameterizedTest(name = "should process update message: {0} expectedResult: {1}, calls to case refresh: {2}")
  @CsvSource(value = ["true,PROCESSED,1", "false,FAILED,1"])
  fun `should refresh case and process updated message when updateAccommodationRecordWithCprAddressUpdate is true`(
    shouldUpdate: Boolean,
    expectedResult: InboxEventHandler.Result,
    count: Int,
  ) {
    val address =
      CanonicalAddress(cprAddressId = "id", status = CanonicalAddressStatus(code = "NotProposed"))
    val proposedAccommodationEntity = buildProposedAccommodationEntity(caseId = caseEntity.id)

    every { caseRepository.findByCrn(crn) } returns caseEntity
    every { proposedAccommodationRepository.findWithNotesByCprAddressId(any()) } returns proposedAccommodationEntity
    every { corePersonRecordClient.getProbationAddress(URI.create(domainEvent.detailUrl!!)) } returns address
    every {
      accommodationSyncService.updateAccommodationRecordWithCprAddressUpdate(
        any(),
        any(),
        any(),
      )
    } returns shouldUpdate

    assertThat(handler.handle(inboxEvent)).isEqualTo(expectedResult)
    verify(exactly = count) { caseRefreshRequestService.requestLiveRefresh(caseEntity.id) }
    verify(exactly = count) {
      accommodationSyncService.updateAccommodationRecordWithCprAddressUpdate(
        crn = crn,
        sasAccommodationRecord = proposedAccommodationEntity,
        cprAddressRecord = address,
      )
    }
  }
}
