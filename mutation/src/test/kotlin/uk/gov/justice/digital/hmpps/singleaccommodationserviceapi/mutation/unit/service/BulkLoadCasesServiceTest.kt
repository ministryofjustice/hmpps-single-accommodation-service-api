package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.unit.service

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.BulkLoadCasesErrorDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.UpstreamFailureType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.ErrorDetail
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.FailureType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.OrchestrationResultDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.UpstreamFailure
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.sasanddelius.TeamCase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.BulkLoadCasesService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseApplicationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseRefreshRequestService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CrnToPrisonNumber
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.TeamCaseOrchestrationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.TeamCodesRequiredException
import java.util.UUID

@ExtendWith(MockKExtension::class)
class BulkLoadCasesServiceTest {

  @MockK
  private lateinit var teamCaseOrchestrationService: TeamCaseOrchestrationService

  @RelaxedMockK
  private lateinit var caseApplicationService: CaseApplicationService

  @MockK
  private lateinit var caseRepository: CaseRepository

  @RelaxedMockK
  private lateinit var caseRefreshRequestService: CaseRefreshRequestService

  @InjectMockKs
  private lateinit var bulkLoadCasesService: BulkLoadCasesService

  private val teamCode = "TEAM1"

  @Test
  fun `creates the cases it does not hold and requests refreshes`() {
    val caseIds = listOf(UUID.randomUUID(), UUID.randomUUID())
    stubTeamCases(TeamCase(crn = "CRN1", prisonNumber = "PN1"), TeamCase(crn = "CRN2", prisonNumber = null))
    every { caseRepository.findUnpersistedCrns(any()) } returns listOf("CRN2")
    every { caseRepository.findByCrns(listOf("CRN1", "CRN2")) } returns caseIds.map { buildCaseEntity(id = it) }

    val result = bulkLoadCasesService.bulkLoadCases(listOf(teamCode), dryRun = false).data

    verify(exactly = 1) {
      caseApplicationService.createCases(
        listOf(
          CrnToPrisonNumber(crn = "CRN1", prisonNumber = "PN1"),
          CrnToPrisonNumber(crn = "CRN2", prisonNumber = null),
        ),
      )
    }
    verify(exactly = 1) { caseRefreshRequestService.requestBulkRefresh(caseIds) }
    assertThat(result.teamsProcessed).isEqualTo(1)
    assertThat(result.crnsFound).isEqualTo(2)
    assertThat(result.casesAlreadyPresent).isEqualTo(1)
    assertThat(result.casesCreated).isEqualTo(1)
    assertThat(result.refreshesRequested).isEqualTo(2)
  }

  @Test
  fun `does nothing when the team has no cases`() {
    stubTeamCases()

    val result = bulkLoadCasesService.bulkLoadCases(listOf(teamCode), dryRun = false).data

    verify(exactly = 0) { caseApplicationService.createCases(any()) }
    verify(exactly = 0) { caseRefreshRequestService.requestBulkRefresh(any()) }
    assertThat(result.teamsProcessed).isEqualTo(1)
    assertThat(result.crnsFound).isZero()
  }

  @Test
  fun `a dry run reports what it would do without writing anything`() {
    stubTeamCases(TeamCase(crn = "CRN1", prisonNumber = null), TeamCase(crn = "CRN2", prisonNumber = null))
    every { caseRepository.findUnpersistedCrns(any()) } returns listOf("CRN2")

    val result = bulkLoadCasesService.bulkLoadCases(listOf(teamCode), dryRun = true).data

    verify(exactly = 0) { caseApplicationService.createCases(any()) }
    verify(exactly = 0) { caseRefreshRequestService.requestBulkRefresh(any()) }
    verify(exactly = 0) { caseRepository.findByCrns(any()) }
    assertThat(result.dryRun).isTrue()
    assertThat(result.crnsFound).isEqualTo(2)
    assertThat(result.casesAlreadyPresent).isEqualTo(1)
    assertThat(result.refreshesRequested).isZero()
  }

  @Test
  fun `skips a team whose cases could not be retrieved and reports the upstream failure`() {
    every { teamCaseOrchestrationService.getCasesByTeamCode(teamCode) } returns OrchestrationResultDto(
      data = emptyList(),
      upstreamFailures = listOf(
        UpstreamFailure(
          callKey = "getCasesByTeamCode$teamCode",
          type = FailureType.TIMEOUT,
          errorDetail = ErrorDetail(httpStatus = null, message = "Request timed out"),
        ),
      ),
    )

    val response = bulkLoadCasesService.bulkLoadCases(listOf(teamCode), dryRun = false)

    verify(exactly = 0) { caseApplicationService.createCases(any()) }
    verify(exactly = 0) { caseRefreshRequestService.requestBulkRefresh(any()) }
    assertThat(response.data.teamsProcessed).isZero()
    assertThat(response.data.errors).isEmpty()
    assertThat(response.upstreamFailures).hasSize(1)
    assertThat(response.upstreamFailures.first().failureType).isEqualTo(UpstreamFailureType.TIMEOUT)
    assertThat(response.upstreamFailures.first().endpoint).isEqualTo("getCasesByTeamCode$teamCode")
  }

  @Test
  fun `records a failing team as an error and carries on with the rest`() {
    stubTeamCases(TeamCase(crn = "CRN1", prisonNumber = null), teamCode = "BROKENTEAM")
    stubTeamCases(TeamCase(crn = "CRN2", prisonNumber = null), teamCode = "OKTEAM")
    every { caseRepository.findUnpersistedCrns(any()) } returns emptyList()
    every { caseRepository.findByCrns(listOf("CRN1")) } throws RuntimeException("error inserting case for this team")
    every { caseRepository.findByCrns(listOf("CRN2")) } returns listOf(buildCaseEntity())

    val result = bulkLoadCasesService.bulkLoadCases(listOf("BROKENTEAM", "OKTEAM"), dryRun = false).data

    assertThat(result.teamsProcessed).isEqualTo(1)
    assertThat(result.errors).containsExactly(BulkLoadCasesErrorDto(teamCode = "BROKENTEAM", message = "error inserting case for this team"))
    assertThat(result.refreshesRequested).isEqualTo(1)
  }

  @Test
  fun `aggregates the counts across every team`() {
    stubTeamCases(TeamCase(crn = "CRN1", prisonNumber = null), teamCode = "TEAM1")
    stubTeamCases(TeamCase(crn = "CRN2", prisonNumber = null), TeamCase(crn = "CRN3", prisonNumber = null), teamCode = "TEAM2")
    every { caseRepository.findUnpersistedCrns(any()) } returns emptyList()
    every { caseRepository.findByCrns(any()) } answers {
      firstArg<List<String>>().map { buildCaseEntity() }
    }

    val result = bulkLoadCasesService.bulkLoadCases(listOf("TEAM1", "TEAM2"), dryRun = false).data

    assertThat(result.teamsProcessed).isEqualTo(2)
    assertThat(result.crnsFound).isEqualTo(3)
    assertThat(result.refreshesRequested).isEqualTo(3)
  }

  @Test
  fun `trims, uppercases and de-duplicates the team codes before use`() {
    stubTeamCases(TeamCase(crn = "CRN1", prisonNumber = null))
    every { caseRepository.findUnpersistedCrns(any()) } returns emptyList()

    val result = bulkLoadCasesService.bulkLoadCases(listOf(" team1 ", "TEAM1"), dryRun = true).data

    verify(exactly = 1) { teamCaseOrchestrationService.getCasesByTeamCode(teamCode) }
    assertThat(result.teamsProcessed).isEqualTo(1)
    assertThat(result.crnsFound).isEqualTo(1)
  }

  @Test
  fun `fails when no usable team codes are supplied`() {
    assertThrows<TeamCodesRequiredException> {
      bulkLoadCasesService.bulkLoadCases(listOf("", "  "), dryRun = false)
    }

    verify(exactly = 0) { teamCaseOrchestrationService.getCasesByTeamCode(any()) }
    verify(exactly = 0) { caseApplicationService.createCases(any()) }
  }

  @Test
  fun `fails before doing anything when the case refresh mechanism is not enabled`() {
    val service = BulkLoadCasesService(
      teamCaseOrchestrationService = teamCaseOrchestrationService,
      caseApplicationService = caseApplicationService,
      caseRepository = caseRepository,
      caseRefreshRequestService = null,
    )

    val exception = assertThrows<IllegalStateException> {
      service.bulkLoadCases(listOf(teamCode), dryRun = false)
    }

    assertThat(exception.message).contains("not enabled")
    verify(exactly = 0) { teamCaseOrchestrationService.getCasesByTeamCode(any()) }
    verify(exactly = 0) { caseApplicationService.createCases(any()) }
  }

  private fun stubTeamCases(vararg cases: TeamCase, teamCode: String = this.teamCode) {
    every { teamCaseOrchestrationService.getCasesByTeamCode(teamCode) } returns OrchestrationResultDto(
      data = cases.toList(),
      upstreamFailures = emptyList(),
    )
  }
}
