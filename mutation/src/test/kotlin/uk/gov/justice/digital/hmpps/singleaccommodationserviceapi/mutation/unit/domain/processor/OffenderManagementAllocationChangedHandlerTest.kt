package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.unit.domain.processor

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecordClient
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.Identifiers
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.IncomingHmppsDomainEventType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.PersonIdentifier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.PersonReference
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.SnsDomainEvent
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.UserEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.UserRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseApplicationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseRefreshRequestService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.InboxEventHandler
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.InboxEventHelper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.handler.OffenderManagementAllocationChangedHandler
import java.time.OffsetDateTime
import java.util.UUID

@ExtendWith(MockKExtension::class)
class OffenderManagementAllocationChangedHandlerTest {

  @RelaxedMockK
  private lateinit var caseApplicationService: CaseApplicationService

  @RelaxedMockK
  private lateinit var inboxEventHelper: InboxEventHelper

  @RelaxedMockK
  private lateinit var userRepository: UserRepository

  @RelaxedMockK
  private lateinit var caseRepository: CaseRepository

  @RelaxedMockK
  private lateinit var caseRefreshRequestService: CaseRefreshRequestService

  @RelaxedMockK
  private lateinit var corePersonRecordClient: CorePersonRecordClient

  @InjectMockKs
  private lateinit var offenderManagementAllocationChangedHandler: OffenderManagementAllocationChangedHandler

  private val prisonNumber = "A1234BC"
  private val staffCode = 99999123L
  private val crn = "X123456"

  private val inboxEvent = InboxEventHandler.InboxEvent(
    id = UUID.randomUUID(),
    eventDetailUrl = "localhost",
    payload = "payload",
  )

  private val eventTypeName = IncomingHmppsDomainEventType.OFFENDER_MANAGEMENT_ALLOCATION_CHANGED.typeName

  @BeforeEach
  fun setUp() {
    every { inboxEventHelper.findPrisonNumber(any()) } returns prisonNumber
  }

  @Test
  fun `supports OFFENDER_MANAGEMENT_ALLOCATION_CHANGED event type`() {
    val expectedEventTypes = setOf(eventTypeName)
    assertThat(offenderManagementAllocationChangedHandler.supportedEventTypes()).containsExactlyElementsOf(expectedEventTypes)
  }

  @Test
  fun `OFFENDER_MANAGEMENT_ALLOCATION_CHANGED partition key is prison number`() {
    assertThat(offenderManagementAllocationChangedHandler.getPartitionKey(inboxEvent)).isEqualTo(prisonNumber)
  }

  @Test
  fun `should refresh case and process OFFENDER_MANAGEMENT_ALLOCATION_CHANGED message when case is known`() {
    val caseId = UUID.randomUUID()
    val caseEntity = mockk<CaseEntity>()

    every { caseRepository.findByPrisonNumber(prisonNumber) } returns caseEntity
    every { caseEntity.id } returns caseId

    assertThat(offenderManagementAllocationChangedHandler.handle(inboxEvent)).isEqualTo(InboxEventHandler.Result.PROCESSED)
    verify(exactly = 1) { caseRefreshRequestService.requestLiveRefresh(caseId) }
    verify(exactly = 0) { caseApplicationService.upsertCase(any(), any()) }
  }

  @Test
  fun `should process OFFENDER_MANAGEMENT_ALLOCATION_CHANGED message when case is known and refresh request service is null`() {
    offenderManagementAllocationChangedHandler = OffenderManagementAllocationChangedHandler(
      caseApplicationService = caseApplicationService,
      inboxEventHelper = inboxEventHelper,
      userRepository = userRepository,
      caseRepository = caseRepository,
      caseRefreshRequestService = null,
      corePersonRecordClient = corePersonRecordClient,
    )

    every { caseRepository.findByPrisonNumber(prisonNumber) } returns mockk()

    assertThat(offenderManagementAllocationChangedHandler.handle(inboxEvent)).isEqualTo(InboxEventHandler.Result.PROCESSED)
    verify(exactly = 0) { caseRefreshRequestService.requestLiveRefresh(any()) }
  }

  @Test
  fun `should ignore OFFENDER_MANAGEMENT_ALLOCATION_CHANGED message when case is unknown and user does not exist`() {
    every { caseRepository.findByPrisonNumber(prisonNumber) } returns null
    every { inboxEventHelper.toDomainEvent(any()) } returns offenderAllocationChangedEvent()
    every { userRepository.findByNomisStaffId(staffCode) } returns null

    assertThat(offenderManagementAllocationChangedHandler.handle(inboxEvent)).isEqualTo(InboxEventHandler.Result.IGNORED)
    verify(exactly = 0) { corePersonRecordClient.getByPrisonNumber(any()) }
    verify(exactly = 0) { caseApplicationService.upsertCase(any(), any()) }
  }

  @Test
  fun `should create case and process OFFENDER_MANAGEMENT_ALLOCATION_CHANGED message when case is unknown and allocated user exists`() {
    every { caseRepository.findByPrisonNumber(prisonNumber) } returns null
    every { inboxEventHelper.toDomainEvent(any()) } returns offenderAllocationChangedEvent()
    every { userRepository.findByNomisStaffId(staffCode) } returns mockk<UserEntity>()
    every { corePersonRecordClient.getByPrisonNumber(prisonNumber) } returns CorePersonRecord(
      identifiers = Identifiers(crns = listOf(crn)),
    )

    assertThat(offenderManagementAllocationChangedHandler.handle(inboxEvent)).isEqualTo(InboxEventHandler.Result.PROCESSED)
    verify(exactly = 1) { caseApplicationService.upsertCase(crn, prisonNumber) }
    verify(exactly = 0) { caseRefreshRequestService.requestLiveRefresh(any()) }
  }

  @Test
  fun `should throw when cpr identifiers are missing`() {
    every { caseRepository.findByPrisonNumber(prisonNumber) } returns null
    every { inboxEventHelper.toDomainEvent(any()) } returns offenderAllocationChangedEvent()
    every { userRepository.findByNomisStaffId(staffCode) } returns mockk<UserEntity>()
    every { corePersonRecordClient.getByPrisonNumber(prisonNumber) } returns CorePersonRecord(identifiers = null)

    assertThatThrownBy { offenderManagementAllocationChangedHandler.handle(inboxEvent) }
      .isInstanceOf(IllegalArgumentException::class.java)
      .hasMessage("This requires a single CRN in cpr identifiers for prisonNumber: [$prisonNumber].")

    verify(exactly = 0) { caseApplicationService.upsertCase(any(), any()) }
  }

  @Test
  fun `should throw when cpr response has more than one CRN`() {
    every { caseRepository.findByPrisonNumber(prisonNumber) } returns null
    every { inboxEventHelper.toDomainEvent(any()) } returns offenderAllocationChangedEvent()
    every { userRepository.findByNomisStaffId(staffCode) } returns mockk<UserEntity>()
    every { corePersonRecordClient.getByPrisonNumber(prisonNumber) } returns CorePersonRecord(
      identifiers = Identifiers(crns = listOf("X111111", "X222222")),
    )

    assertThatThrownBy { offenderManagementAllocationChangedHandler.handle(inboxEvent) }
      .isInstanceOf(IllegalArgumentException::class.java)
      .hasMessage("This requires a single CRN in cpr identifiers for prisonNumber: [$prisonNumber].")

    verify(exactly = 0) { caseApplicationService.upsertCase(any(), any()) }
  }

  private fun offenderAllocationChangedEvent() = SnsDomainEvent(
    eventType = eventTypeName,
    version = 1,
    occurredAt = OffsetDateTime.now(),
    personReference = PersonReference(
      identifiers = listOf(PersonIdentifier(type = "NOMS", value = prisonNumber)),
    ),
    additionalInformation = mapOf("staffCode" to staffCode),
  )
}
