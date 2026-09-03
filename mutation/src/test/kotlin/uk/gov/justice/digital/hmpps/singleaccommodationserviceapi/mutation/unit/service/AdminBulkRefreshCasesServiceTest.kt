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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withCrn
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.AdminBulkRefreshCasesService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseRefreshRequestService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.CrnsRequiredException
import java.util.UUID

@ExtendWith(MockKExtension::class)
class AdminBulkRefreshCasesServiceTest {

  @MockK
  private lateinit var caseRepository: CaseRepository

  @RelaxedMockK
  private lateinit var caseRefreshRequestService: CaseRefreshRequestService

  @InjectMockKs
  private lateinit var adminBulkRefreshCasesService: AdminBulkRefreshCasesService

  private val caseId = UUID.randomUUID()
  private val otherCaseId = UUID.randomUUID()

  @Test
  fun `stages a refresh for every case holding one of the crns`() {
    stubCases("CRN1" to caseId, "CRN2" to otherCaseId)

    val result = adminBulkRefreshCasesService.bulkRefreshCasesByCrn(listOf("CRN1", "CRN2"), dryRun = false).data

    verify(exactly = 1) { caseRefreshRequestService.requestBulkRefresh(listOf(caseId, otherCaseId), resetAttempts = true) }
    assertThat(result.crnsRequested).isEqualTo(2)
    assertThat(result.casesFound).isEqualTo(2)
    assertThat(result.refreshesRequested).isEqualTo(2)
    assertThat(result.crnsNotFound).isEmpty()
  }

  @Test
  fun `reports the crns held by no case and refreshes the rest`() {
    stubCases("CRN1" to caseId)

    val result = adminBulkRefreshCasesService.bulkRefreshCasesByCrn(listOf("CRN1", "CRN2"), dryRun = false).data

    verify(exactly = 1) { caseRefreshRequestService.requestBulkRefresh(listOf(caseId), resetAttempts = true) }
    assertThat(result.casesFound).isEqualTo(1)
    assertThat(result.refreshesRequested).isEqualTo(1)
    assertThat(result.crnsNotFound).containsExactly("CRN2")
  }

  @Test
  fun `trims, uppercases and deduplicates the crns before use`() {
    stubCases("CRN1" to caseId)

    val result = adminBulkRefreshCasesService.bulkRefreshCasesByCrn(listOf(" crn1 ", "CRN1", ""), dryRun = false).data

    verify(exactly = 1) { caseRepository.mapByCrns(listOf("CRN1")) }
    assertThat(result.crnsRequested).isEqualTo(1)
    assertThat(result.casesFound).isEqualTo(1)
  }

  @Test
  fun `reports what a run would do without staging anything when it is a dry run`() {
    stubCases("CRN1" to caseId)

    val result = adminBulkRefreshCasesService.bulkRefreshCasesByCrn(listOf("CRN1", "CRN2"), dryRun = true).data

    verify(exactly = 0) { caseRefreshRequestService.requestBulkRefresh(any(), any()) }
    assertThat(result.dryRun).isTrue()
    assertThat(result.casesFound).isEqualTo(1)
    assertThat(result.refreshesRequested).isZero()
    assertThat(result.crnsNotFound).containsExactly("CRN2")
  }

  @Test
  fun `fails when no usable crns are supplied`() {
    assertThrows<CrnsRequiredException> {
      adminBulkRefreshCasesService.bulkRefreshCasesByCrn(listOf("", "  "), dryRun = false)
    }

    verify(exactly = 0) { caseRepository.mapByCrns(any()) }
    verify(exactly = 0) { caseRefreshRequestService.requestBulkRefresh(any(), any()) }
  }

  @Test
  fun `fails before doing anything when the case refresh mechanism is not enabled`() {
    val service = AdminBulkRefreshCasesService(
      caseRepository = caseRepository,
      caseRefreshRequestService = null,
    )

    val exception = assertThrows<IllegalStateException> { service.bulkRefreshCasesByCrn(listOf("CRN1"), dryRun = false) }

    assertThat(exception.message).contains("not enabled")
    verify(exactly = 0) { caseRepository.mapByCrns(any()) }
  }

  private fun stubCases(vararg cases: Pair<String, UUID>) {
    every { caseRepository.mapByCrns(any()) } returns cases.associate { (crn, id) ->
      crn to buildCaseEntity(id = id) { withCrn(crn) }
    }
  }
}
