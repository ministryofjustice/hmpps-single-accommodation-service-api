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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.probation.AddressStatusCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildProposedAccommodationEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withCrn
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.PersonIdentifier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.PersonReference
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.SnsDomainEvent
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.ProposedAccommodationRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.AccommodationSyncService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseRefreshRequestService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.InboxEventHandler
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.InboxEventHelper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.handler.CprProbationAddressCreatedHandler
import java.net.URI
import java.time.OffsetDateTime
import java.util.UUID

@ExtendWith(MockKExtension::class)
class CprProbationAddressCreatedHandlerTest {
  @MockK
  private lateinit var accommodationSyncService: AccommodationSyncService

  @MockK
  private lateinit var proposedAccommodationRepository: ProposedAccommodationRepository

  @MockK
  private lateinit var caseRepository: CaseRepository

  @MockK
  private lateinit var corePersonRecordClient: CorePersonRecordClient

  @MockK
  private lateinit var inboxEventHelper: InboxEventHelper

  @RelaxedMockK
  private lateinit var caseRefreshRequestService: CaseRefreshRequestService

  @InjectMockKs
  private lateinit var handler: CprProbationAddressCreatedHandler

  val crn = UUID.randomUUID().toString()
  val domainEvent =
    SnsDomainEvent(
      eventType = "core-person-record.probation.address.created",
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
  val inboxEvent = InboxEventHandler.InboxEvent(
    id = UUID.randomUUID(),
    eventDetailUrl = "localhost",
    payload = "payload",
  )

  @BeforeEach
  fun setup() {
    every { inboxEventHelper.toDomainEvent(inboxEvent) } returns domainEvent
    every { inboxEventHelper.findCrn(inboxEvent) } returns crn
  }

  @Test
  fun `should not refresh case and should ignore created message when case is not known`() {
    val caseId = UUID.randomUUID()
    every { caseRepository.findByCrn(crn) } returns null
    assertThat(handler.handle(inboxEvent)).isEqualTo(InboxEventHandler.Result.IGNORED)
    verify(exactly = 0) { caseRefreshRequestService.requestLiveRefresh(caseId) }
  }

  @Test
  fun `should refresh case and ignore created message when address is not proposed type`() {
    val caseEntity = buildCaseEntity { withCrn(crn) }
    val address =
      CanonicalAddress(cprAddressId = "id", status = CanonicalAddressStatus(code = "NotProposed"))

    every { caseRepository.findByCrn(crn) } returns caseEntity
    every { corePersonRecordClient.getProbationAddress(URI.create(domainEvent.detailUrl!!)) } returns address

    assertThat(handler.handle(inboxEvent)).isEqualTo(InboxEventHandler.Result.IGNORED)
    verify(exactly = 1) { caseRefreshRequestService.requestLiveRefresh(caseEntity.id) }
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource(value = ["true,PROCESSED,1", "false,FAILED,1"])
  fun `should regresh case and process created message when updateAccommodationRecordWithCprAddressUpdate is true`(
    shouldUpdate: Boolean,
    expectedResult: InboxEventHandler.Result,
    count: Int,
  ) {
    val caseEntity = buildCaseEntity { withCrn(crn) }
    val cprAddressId = UUID.randomUUID()
    val address =
      CanonicalAddress(
        cprAddressId = cprAddressId.toString(),
        status = CanonicalAddressStatus(code = AddressStatusCode.PR.name),
      )
    val proposedAccommodationEntity = buildProposedAccommodationEntity()

    every { caseRepository.findByCrn(crn) } returns caseEntity
    every { corePersonRecordClient.getProbationAddress(URI.create(domainEvent.detailUrl!!)) } returns address
    every { proposedAccommodationRepository.findWithNotesByCprAddressId(any()) } returns proposedAccommodationEntity
    every {
      accommodationSyncService.updateAccommodationRecordWithCprAddressUpdate(
        any(),
        any(),
        any(),
      )
    } returns shouldUpdate

    assertThat(handler.handle(inboxEvent)).isEqualTo(expectedResult)
    verify(exactly = count) { caseRefreshRequestService.requestLiveRefresh(caseEntity.id) }
  }

  @ParameterizedTest(name = "should process create message: {0} expectedResult: {1}, calls to case refresh: {2}")
  @CsvSource(value = ["true,PROCESSED,1", "false,FAILED,1"])
  fun `should refresh case and process created message when createAccommodationRecordWithCprAddressCreate is true`(
    shouldCreate: Boolean,
    expectedResult: InboxEventHandler.Result,
    count: Int,
  ) {
    val caseEntity = buildCaseEntity { withCrn(crn) }
    val cprAddressId = UUID.randomUUID()
    val address =
      CanonicalAddress(
        cprAddressId = cprAddressId.toString(),
        status = CanonicalAddressStatus(code = AddressStatusCode.PR.name),
      )

    every { caseRepository.findByCrn(crn) } returns caseEntity
    every { corePersonRecordClient.getProbationAddress(URI.create(domainEvent.detailUrl!!)) } returns address
    every { proposedAccommodationRepository.findWithNotesByCprAddressId(any()) } returns null
    every {
      accommodationSyncService.createAccommodationRecordWithCprAddressCreate(
        any(),
        any(),
        any(),
      )
    } returns shouldCreate

    assertThat(handler.handle(inboxEvent)).isEqualTo(expectedResult)
    verify(exactly = count) { caseRefreshRequestService.requestLiveRefresh(caseEntity.id) }
  }
}
