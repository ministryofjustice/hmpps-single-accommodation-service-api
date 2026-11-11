package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.unit.service

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import io.mockk.verifyOrder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withCrn
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseRefreshRequestEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRefreshRequestRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.AdminArchiveCasesService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.CrnsRequiredException
import java.util.UUID

@ExtendWith(MockKExtension::class)
class AdminArchiveCasesServiceTest {

  @MockK
  private lateinit var caseRepository: CaseRepository

  @MockK
  private lateinit var caseRefreshRequestRepository: CaseRefreshRequestRepository

  @InjectMockKs
  private lateinit var adminArchiveCasesService: AdminArchiveCasesService

  private val caseId = UUID.randomUUID()
  private val otherCaseId = UUID.randomUUID()

  @Test
  fun `deletes the refresh request then archives every case holding one of the crns`() {
    stubCases("CRN1" to caseId, "CRN2" to otherCaseId)
    val refreshRequest = mockk<CaseRefreshRequestEntity>()
    every { caseRefreshRequestRepository.findByCaseId(caseId) } returns refreshRequest
    every { caseRefreshRequestRepository.findByCaseId(otherCaseId) } returns null
    every { caseRefreshRequestRepository.delete(refreshRequest) } just runs
    every { caseRepository.archiveById(any()) } returns 1

    val result = adminArchiveCasesService.archiveCasesByCrn(listOf("CRN1", "CRN2"), dryRun = false).data

    verifyOrder {
      caseRefreshRequestRepository.findByCaseId(caseId)
      caseRefreshRequestRepository.delete(refreshRequest)
      caseRepository.archiveById(caseId)
    }
    verify(exactly = 1) { caseRepository.archiveById(otherCaseId) }
    assertThat(result.dryRun).isFalse()
    assertThat(result.crnsRequested).isEqualTo(2)
    assertThat(result.casesFound).isEqualTo(2)
    assertThat(result.casesArchived).isEqualTo(2)
    assertThat(result.refreshRequestsDeleted).isEqualTo(1)
    assertThat(result.crnsNotFound).isEmpty()
  }

  @Test
  fun `reports the crns held by no case and archives the rest`() {
    stubCases("CRN1" to caseId)
    every { caseRefreshRequestRepository.findByCaseId(caseId) } returns null
    every { caseRepository.archiveById(caseId) } returns 1

    val result = adminArchiveCasesService.archiveCasesByCrn(listOf("CRN1", "CRN2"), dryRun = false).data

    assertThat(result.casesFound).isEqualTo(1)
    assertThat(result.casesArchived).isEqualTo(1)
    assertThat(result.crnsNotFound).containsExactly("CRN2")
  }

  @Test
  fun `trims, uppercases and deduplicates the crns before use`() {
    stubCases("CRN1" to caseId)
    every { caseRefreshRequestRepository.findByCaseId(caseId) } returns null
    every { caseRepository.archiveById(caseId) } returns 1

    val result = adminArchiveCasesService.archiveCasesByCrn(listOf(" crn1 ", "CRN1", ""), dryRun = false).data

    verify(exactly = 1) { caseRepository.mapByCrns(listOf("CRN1")) }
    assertThat(result.crnsRequested).isEqualTo(1)
    assertThat(result.casesFound).isEqualTo(1)
  }

  @Test
  fun `reports what a run would do without archiving anything when it is a dry run`() {
    stubCases("CRN1" to caseId)

    val result = adminArchiveCasesService.archiveCasesByCrn(listOf("CRN1", "CRN2"), dryRun = true).data

    verify(exactly = 0) { caseRefreshRequestRepository.findByCaseId(any()) }
    verify(exactly = 0) { caseRepository.archiveById(any()) }
    assertThat(result.dryRun).isTrue()
    assertThat(result.casesFound).isEqualTo(1)
    assertThat(result.casesArchived).isZero()
    assertThat(result.refreshRequestsDeleted).isZero()
    assertThat(result.crnsNotFound).containsExactly("CRN2")
  }

  @Test
  fun `fails when no usable crns are supplied`() {
    assertThrows<CrnsRequiredException> {
      adminArchiveCasesService.archiveCasesByCrn(listOf("", "  "), dryRun = false)
    }

    verify(exactly = 0) { caseRepository.mapByCrns(any()) }
    verify(exactly = 0) { caseRepository.archiveById(any()) }
  }

  private fun stubCases(vararg cases: Pair<String, UUID>) {
    every { caseRepository.mapByCrns(any()) } returns cases.associate { (crn, id) ->
      crn to buildCaseEntity(id = id) { withCrn(crn) }
    }
  }
}
