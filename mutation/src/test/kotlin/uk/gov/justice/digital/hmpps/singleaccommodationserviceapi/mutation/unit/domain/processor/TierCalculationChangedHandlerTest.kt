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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseRefreshRequestService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.InboxEventHandler
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.InboxEventHelper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.handler.TierCalculationChangedHandler
import java.util.UUID

@ExtendWith(MockKExtension::class)
class TierCalculationChangedHandlerTest {

  @RelaxedMockK
  private lateinit var caseRepository: CaseRepository

  @RelaxedMockK
  private lateinit var inboxEventHelper: InboxEventHelper

  @RelaxedMockK
  private lateinit var caseRefreshRequestService: CaseRefreshRequestService

  @InjectMockKs
  private lateinit var tierCalculationChangedHandler: TierCalculationChangedHandler

  val crn = UUID.randomUUID().toString()

  val inboxEvent = InboxEventHandler.InboxEvent(
    id = UUID.randomUUID(),
    eventDetailUrl = "localhost",
    payload = "payload",
  )

  @Test
  fun `should refresh case and process message when case is known`() {
    val caseId = UUID.randomUUID()
    val caseEntity = mockk<CaseEntity>()

    every { caseRepository.findByCrn(crn) } returns caseEntity
    every { caseEntity.id } returns caseId
    every { inboxEventHelper.findCrn(any()) } returns crn

    assertThat(tierCalculationChangedHandler.handle(inboxEvent)).isEqualTo(InboxEventHandler.Result.PROCESSED)
    verify(exactly = 1) { caseRefreshRequestService.requestLiveRefresh(caseId) }
  }

  @Test
  fun `should not refresh case and ignore message when case is not known`() {
    every { caseRepository.findByCrn(crn) } returns null
    every { inboxEventHelper.findCrn(any()) } returns crn

    assertThat(tierCalculationChangedHandler.handle(inboxEvent)).isEqualTo(InboxEventHandler.Result.IGNORED)
    verify(exactly = 0) { caseRefreshRequestService.requestLiveRefresh(any()) }
  }

  @Test
  fun `should not refresh case and should process message when inbox event handler is null`() {
    tierCalculationChangedHandler =
      TierCalculationChangedHandler(caseRepository, null, inboxEventHelper)

    assertThat(tierCalculationChangedHandler.handle(inboxEvent)).isEqualTo(InboxEventHandler.Result.PROCESSED)
    verify(exactly = 0) { caseRefreshRequestService.requestLiveRefresh(any()) }
  }
}
