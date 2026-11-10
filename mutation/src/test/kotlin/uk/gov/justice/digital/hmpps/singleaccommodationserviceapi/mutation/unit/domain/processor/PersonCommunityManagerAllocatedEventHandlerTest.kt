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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildManager
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildTeam
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withCrn
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseRefreshPriority
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.OnboardedTeamRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseApplicationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseRefreshRequestService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CrnToPrisonNumber
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.InboxEventHandler
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.InboxEventHelper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.handler.PersonCommunityManagerAllocatedEventHandler
import java.util.UUID

@ExtendWith(MockKExtension::class)
class PersonCommunityManagerAllocatedEventHandlerTest {

  @RelaxedMockK
  private lateinit var caseApplicationService: CaseApplicationService

  @MockK
  private lateinit var inboxEventHelper: InboxEventHelper

  @MockK
  private lateinit var approvedPremisesAndDeliusClient: ApprovedPremisesAndDeliusClient

  @MockK
  private lateinit var onboardedTeamRepository: OnboardedTeamRepository

  @RelaxedMockK
  private lateinit var caseRepository: CaseRepository

  @RelaxedMockK
  private lateinit var caseRefreshRequestService: CaseRefreshRequestService

  @InjectMockKs
  private lateinit var personCommunityManagerAllocatedEventHandler: PersonCommunityManagerAllocatedEventHandler

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
    every { caseRepository.findByCrn(crn) } returns null
  }

  @Test
  fun `should request a live refresh when the case already exists`() {
    val caseEntity = buildCaseEntity { withCrn(crn) }
    every { caseRepository.findByCrn(crn) } returns caseEntity

    assertThat(personCommunityManagerAllocatedEventHandler.handle(inboxEvent)).isEqualTo(InboxEventHandler.Result.PROCESSED)

    verify(exactly = 1) { caseRefreshRequestService.requestLiveRefresh(caseEntity.id) }
    verify(exactly = 0) { approvedPremisesAndDeliusClient.postCaseSummaries(any()) }
    verify(exactly = 0) { caseApplicationService.createBlankCases(any(), any()) }
  }

  @Test
  fun `should create a blank case with a live refresh when it is allocated to an onboarded team`() {
    stubCaseAllocatedToTeam("TEAM1")
    every { onboardedTeamRepository.existsByTeamCodeIsIgnoreCase("TEAM1") } returns true

    assertThat(personCommunityManagerAllocatedEventHandler.handle(inboxEvent)).isEqualTo(InboxEventHandler.Result.PROCESSED)
    verify(exactly = 1) {
      caseApplicationService.createBlankCases(
        listOf(CrnToPrisonNumber(crn, nomsId)),
        CaseRefreshPriority.LIVE,
      )
    }
  }

  @Test
  fun `should ignore the event when the case is allocated to a team that is not onboarded`() {
    stubCaseAllocatedToTeam("TEAM2")
    every { onboardedTeamRepository.existsByTeamCodeIsIgnoreCase("TEAM2") } returns false

    assertThat(personCommunityManagerAllocatedEventHandler.handle(inboxEvent)).isEqualTo(InboxEventHandler.Result.IGNORED)
    verify(exactly = 0) { caseApplicationService.createBlankCases(any(), any()) }
  }

  @Test
  fun `should look up the team code case insensitively`() {
    stubCaseAllocatedToTeam("team1")
    every { onboardedTeamRepository.existsByTeamCodeIsIgnoreCase("team1") } returns true

    assertThat(personCommunityManagerAllocatedEventHandler.handle(inboxEvent)).isEqualTo(InboxEventHandler.Result.PROCESSED)
    verify(exactly = 1) { onboardedTeamRepository.existsByTeamCodeIsIgnoreCase("team1") }
  }

  private fun stubCaseAllocatedToTeam(teamCode: String) {
    every { approvedPremisesAndDeliusClient.postCaseSummaries(listOf(crn)) } returns CaseSummaries(
      listOf(buildCaseSummary(crn = crn, nomsId = nomsId, manager = buildManager(team = buildTeam(code = teamCode)))),
    )
  }
}
