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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremisesanddelius.ApprovedPremisesAndDeliusClient
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremisesanddelius.CaseSummaries
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildManager
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildTeam
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.OnboardedTeamRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseCreationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.InboxEventHandler
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.InboxEventHelper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.handler.CaseAllocationHandler
import java.util.UUID

@ExtendWith(MockKExtension::class)
class CaseAllocationHandlerTest {

  @RelaxedMockK
  private lateinit var caseCreationService: CaseCreationService

  @MockK
  private lateinit var inboxEventHelper: InboxEventHelper

  @MockK
  private lateinit var approvedPremisesAndDeliusClient: ApprovedPremisesAndDeliusClient

  @MockK
  private lateinit var onboardedTeamRepository: OnboardedTeamRepository

  @InjectMockKs
  private lateinit var caseAllocationHandler: CaseAllocationHandler

  private val crn = "X123456"
  private val nomsId = "A1234BC"

  private val inboxEvent = InboxEventHandler.InboxEvent(
    id = UUID.randomUUID(),
    eventDetailUrl = "localhost",
    payload = "payload",
  )

  @BeforeEach
  fun setUp() {
    every { inboxEventHelper.findCrn(inboxEvent) } returns crn
  }

  @Test
  fun `should create the case when it is allocated to an onboarded team`() {
    stubCaseAllocatedToTeam("TEAM1")
    every { onboardedTeamRepository.existsById("TEAM1") } returns true

    assertThat(caseAllocationHandler.handle(inboxEvent)).isEqualTo(InboxEventHandler.Result.PROCESSED)
    verify(exactly = 1) { caseCreationService.upsertCase(crn, nomsId) }
  }

  @Test
  fun `should ignore the event when the case is allocated to a team that is not onboarded`() {
    stubCaseAllocatedToTeam("TEAM2")
    every { onboardedTeamRepository.existsById("TEAM2") } returns false

    assertThat(caseAllocationHandler.handle(inboxEvent)).isEqualTo(InboxEventHandler.Result.IGNORED)
    verify(exactly = 0) { caseCreationService.upsertCase(any(), any()) }
  }

  @Test
  fun `should look up the uppercased teamcode as it is stored by the bulk load`() {
    stubCaseAllocatedToTeam("team1")
    every { onboardedTeamRepository.existsById("TEAM1") } returns true

    assertThat(caseAllocationHandler.handle(inboxEvent)).isEqualTo(InboxEventHandler.Result.PROCESSED)
  }

  private fun stubCaseAllocatedToTeam(teamCode: String) {
    every { approvedPremisesAndDeliusClient.postCaseSummaries(listOf(crn)) } returns CaseSummaries(
      listOf(buildCaseSummary(crn = crn, nomsId = nomsId, manager = buildManager(team = buildTeam(code = teamCode)))),
    )
  }
}
