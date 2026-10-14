package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.unit.domain.processor

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.IncomingHmppsDomainEventType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseIdentifierEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.InboxEventHandler
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.InboxEventHelper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.handler.CprProbationRecordDeletedHandler
import java.util.UUID

@ExtendWith(MockKExtension::class)
class CprProbationRecordDeletedHandlerTest {

  @RelaxedMockK
  private lateinit var caseRepository: CaseRepository

  @RelaxedMockK
  private lateinit var inboxEventHelper: InboxEventHelper

  @InjectMockKs
  private lateinit var cprProbationRecordDeletedHandler: CprProbationRecordDeletedHandler

  private val crn = UUID.randomUUID().toString()

  private val inboxEvent = InboxEventHandler.InboxEvent(
    id = UUID.randomUUID(),
    eventDetailUrl = "localhost",
    payload = "payload",
  )

  @Test
  fun `supports CPR probation record deleted event type`() {
    val expectedEventType = IncomingHmppsDomainEventType.CPR_PROBATION_RECORD_DELETED.typeName

    assertThat(cprProbationRecordDeletedHandler.supportedEventTypes()).containsExactly(expectedEventType)
  }

  @Test
  fun `partition key is CRN`() {
    every { inboxEventHelper.findCrn(any()) } returns crn

    assertThat(cprProbationRecordDeletedHandler.getPartitionKey(inboxEvent)).isEqualTo(crn)
  }

  @Test
  fun `should archive crn and process message when case is known`() {
    val caseEntity = mockk<CaseEntity>()
    val caseIdentifier = mockk<CaseIdentifierEntity>()

    every { caseRepository.findByCrn(crn) } returns caseEntity
    every { caseEntity.archiveCrn(crn) } returns caseIdentifier
    every { caseRepository.save(caseEntity) } returns caseEntity
    every { inboxEventHelper.findCrn(any()) } returns crn

    assertThat(cprProbationRecordDeletedHandler.handle(inboxEvent)).isEqualTo(InboxEventHandler.Result.PROCESSED)
    verify(exactly = 1) { caseEntity.archiveCrn(crn) }
    verify(exactly = 1) { caseRepository.save(caseEntity) }
  }

  @Test
  fun `should ignore message when case is not known`() {
    every { caseRepository.findByCrn(crn) } returns null
    every { inboxEventHelper.findCrn(any()) } returns crn

    assertThat(cprProbationRecordDeletedHandler.handle(inboxEvent)).isEqualTo(InboxEventHandler.Result.IGNORED)
    verify(exactly = 0) { caseRepository.save(any()) }
  }
}
