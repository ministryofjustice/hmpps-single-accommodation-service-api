package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.unit.domain.processor

import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import tools.jackson.databind.json.JsonMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.Tier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierClient
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.PersonIdentifier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.PersonReference
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.SnsDomainEvent
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseApplicationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseRefreshRequestService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.InboxEventHandler
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.handler.TierCalculationChangedHandler
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.handler.TierEventHandlerConfig
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.util.UUID

@ExtendWith(MockKExtension::class)
class TierCalculationChangedHandlerTest {

  @RelaxedMockK
  private lateinit var caseApplicationService: CaseApplicationService

  @RelaxedMockK
  private lateinit var tierClient: TierClient

  @RelaxedMockK
  private lateinit var caseRepository: CaseRepository

  @RelaxedMockK
  private lateinit var jsonMapper: JsonMapper

  @RelaxedMockK
  private lateinit var tierEventHandlerConfig: TierEventHandlerConfig

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
  fun `should trigger a case refresh`() {
    val caseId = UUID.randomUUID()
    val caseEntity = mockk<CaseEntity>()
    val tier = Tier(
      tierScore = "A3",
      calculationId = UUID.randomUUID(),
      calculationDate = LocalDateTime.now(),
      changeReason = null,
    )

    every { caseRepository.findByCrn(crn) } returns caseEntity
    every { caseEntity.id } returns caseId
    every { tierEventHandlerConfig.v3Enabled } returns false
    every { tierClient.getTier(crn) } returns tier
    every { jsonMapper.readValue(any<String>(), SnsDomainEvent::class.java) } returns event

    val inboxEvent = InboxEventHandler.InboxEvent(
      id = UUID.randomUUID(),
      eventDetailUrl = "localhost",
      payload = "payload",
    )

    tierCalculationChangedHandler.handle(inboxEvent)
    verify(exactly = 1) { caseRefreshRequestService.requestLiveRefresh(caseId) }
  }
}
