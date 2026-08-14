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
import tools.jackson.databind.json.JsonMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.PersonIdentifier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.PersonReference
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.SnsDomainEvent
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseRefreshRequestService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.InboxEventHandler
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.handler.TierCalculationChangedHandler
import java.time.OffsetDateTime
import java.util.UUID

@ExtendWith(MockKExtension::class)
class TierCalculationChangedHandlerTest {

  @RelaxedMockK
  private lateinit var caseRepository: CaseRepository

  @RelaxedMockK
  private lateinit var jsonMapper: JsonMapper

  @RelaxedMockK
  private lateinit var caseRefreshRequestService: CaseRefreshRequestService

  @InjectMockKs
  private lateinit var tierCalculationChangedHandler: TierCalculationChangedHandler

  val crn = UUID.randomUUID().toString()
  val event =
    SnsDomainEvent(
      eventType = "tier.calculation.changed",
      version = 1,
      description = "test event",
      detailUrl = "localhost",
      occurredAt = OffsetDateTime.now(),
      personReference =
      PersonReference(
        identifiers = listOf(PersonIdentifier("CRN", crn)),
      ),
    )

  @Test
  fun `should process message and trigger a case refresh when case is known`() {
    val caseId = UUID.randomUUID()
    val caseEntity = mockk<CaseEntity>()

    every { caseRepository.findByCrn(crn) } returns caseEntity
    every { caseEntity.id } returns caseId
    every { jsonMapper.readValue(any<String>(), SnsDomainEvent::class.java) } returns event

    val inboxEvent = InboxEventHandler.InboxEvent(
      id = UUID.randomUUID(),
      eventDetailUrl = "localhost",
      payload = "payload",
    )

    assertThat(tierCalculationChangedHandler.handle(inboxEvent)).isEqualTo(InboxEventHandler.Result.PROCESSED)
    verify(exactly = 1) { caseRefreshRequestService.requestLiveRefresh(caseId) }
  }

  @Test
  fun `should ignore message when case is not known`() {
    val caseId = UUID.randomUUID()

    every { caseRepository.findByCrn(crn) } returns null
    every { jsonMapper.readValue(any<String>(), SnsDomainEvent::class.java) } returns event

    val inboxEvent = InboxEventHandler.InboxEvent(
      id = UUID.randomUUID(),
      eventDetailUrl = "localhost",
      payload = "payload",
    )

    assertThat(tierCalculationChangedHandler.handle(inboxEvent)).isEqualTo(InboxEventHandler.Result.IGNORED)
    verify(exactly = 0) { caseRefreshRequestService.requestLiveRefresh(caseId) }
  }
}
