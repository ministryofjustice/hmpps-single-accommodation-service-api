package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.unit.service

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.OrchestrationResultDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper.CaseMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseCreationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseMutationOrchestrationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseMutationOrchestrationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseSnapshotAssembler
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CrnToPrisonNumber
import java.util.UUID

@ExtendWith(MockKExtension::class)
class CaseCreationServiceTest {

  @Nested
  inner class SaveUnpersistedCases {

    @MockK
    lateinit var caseRepository: CaseRepository

    @MockK
    lateinit var entityManager: EntityManager

    @MockK
    lateinit var caseOrchestrationService: CaseMutationOrchestrationService

    @MockK(relaxed = true)
    lateinit var caseSnapshotAssembler: CaseSnapshotAssembler

    private val caseMapper = CaseMapper()

    private lateinit var caseCreationService: CaseCreationService

    @BeforeEach
    fun setUp() {
      caseCreationService = buildService(caseListV2Enabled = false)
    }

    private fun buildService(caseListV2Enabled: Boolean) = CaseCreationService(
      caseOrchestrationService = caseOrchestrationService,
      caseSnapshotAssembler = caseSnapshotAssembler,
      caseRepository = caseRepository,
      caseMapper = caseMapper,
      entityManager = entityManager,
      caseListV2Enabled = caseListV2Enabled,
    )

    private fun stubOrchestrationResult(crn: String) {
      every { caseOrchestrationService.getCurrentCaseResult(crn = crn, prisonNumber = any()) } returns
        OrchestrationResultDto(
          data = CaseMutationOrchestrationDto(
            crn = crn,
            cpr = null,
            tier = null,
            prisoner = null,
            cas1CurrentPremises = null,
            cas3CurrentPremises = null,
            cas1Application = null,
            cas3Application = null,
            case = null,
          ),
        )
    }

    @Nested
    inner class SaveUnpersistedCasesAsBlankRows {
      @Test
      fun `persists only unpersisted Crns`() {
        val first = CrnToPrisonNumber(crn = UUID.randomUUID().toString(), prisonNumber = UUID.randomUUID().toString())
        val second = CrnToPrisonNumber(crn = UUID.randomUUID().toString(), prisonNumber = UUID.randomUUID().toString())
        val third = CrnToPrisonNumber(crn = UUID.randomUUID().toString(), prisonNumber = UUID.randomUUID().toString())
        val crnToPrisonNumbers = listOf(first, second, third)
        val entities = mutableListOf<CaseEntity>()

        every { caseRepository.findUnpersistedCrns(any()) } returns listOf(first.crn, third.crn)
        every { entityManager.persist(capture(entities)) } just runs

        caseCreationService.saveUnpersistedCasesAsBlankRows(crnToPrisonNumbers)

        assertThat(entities).hasSize(2)
        assertThat(entities.map { it.latestCrn() }).containsExactly(first.crn, third.crn)
      }

      @Test
      fun `does not save when no cases to persist`() {
        val crnToPrisonNumbers = List(3) {
          CrnToPrisonNumber(
            crn = UUID.randomUUID().toString(),
            prisonNumber = UUID.randomUUID().toString(),
          )
        }

        every { caseRepository.findUnpersistedCrns(any()) } returns emptyList()

        caseCreationService.saveUnpersistedCasesAsBlankRows(crnToPrisonNumbers)

        verify(exactly = 1) { caseRepository.findUnpersistedCrns(any()) }
        verify(exactly = 0) { entityManager.persist(any()) }
      }
    }

    @Nested
    inner class SaveUnpersistedCases {

      @Test
      fun `persists only unpersisted Crns as blank record when caseListV2Enabled is false`() {
        caseCreationService = buildService(caseListV2Enabled = false)
        val first = CrnToPrisonNumber(crn = UUID.randomUUID().toString(), prisonNumber = UUID.randomUUID().toString())
        val second = CrnToPrisonNumber(crn = UUID.randomUUID().toString(), prisonNumber = UUID.randomUUID().toString())
        val third = CrnToPrisonNumber(crn = UUID.randomUUID().toString(), prisonNumber = UUID.randomUUID().toString())
        val crnToPrisonNumbers = listOf(first, second, third)
        val entities = mutableListOf<CaseEntity>()

        every { caseRepository.findUnpersistedCrns(any()) } returns listOf(first.crn, third.crn)
        every { entityManager.persist(capture(entities)) } just runs

        caseCreationService.saveUnpersistedCasesAsBlankRows(crnToPrisonNumbers)

        verify(exactly = 0) { caseSnapshotAssembler.upsertCase(any(), any()) }

        assertThat(entities).hasSize(2)
        assertThat(entities.map { it.latestCrn() }).containsExactly(first.crn, third.crn)
      }

      @Test
      fun `persists only unpersisted Crns and populates all data on the record when caseListV2Enabled is true`() {
        caseCreationService = buildService(caseListV2Enabled = true)
        val crnToPrisonNumber =
          CrnToPrisonNumber(crn = UUID.randomUUID().toString(), prisonNumber = UUID.randomUUID().toString())

        every { caseRepository.findByIdentifiers(crns = any(), prisonNumbers = any()) } returns null
        every { caseRepository.findUnpersistedCrns(any()) } returns listOf(crnToPrisonNumber.crn)
        every { caseRepository.save(any<CaseEntity>()) } answers { firstArg() }

        stubOrchestrationResult(crnToPrisonNumber.crn)

        caseCreationService.saveUnpersistedCases(listOf(crnToPrisonNumber))

        verify(exactly = 1) { caseSnapshotAssembler.upsertCase(any(), any()) }
      }

      @ParameterizedTest
      @ValueSource(booleans = [true, false])
      fun `does not save when no cases to persist`(v2Enabled: Boolean) {
        caseCreationService = buildService(caseListV2Enabled = v2Enabled)
        val crnToPrisonNumbers = List(3) {
          CrnToPrisonNumber(
            crn = UUID.randomUUID().toString(),
            prisonNumber = UUID.randomUUID().toString(),
          )
        }

        every { caseRepository.findUnpersistedCrns(any()) } returns emptyList()

        caseCreationService.saveUnpersistedCases(crnToPrisonNumbers)

        verify(exactly = 1) { caseRepository.findUnpersistedCrns(any()) }
        verify(exactly = 0) { caseOrchestrationService.getCurrentCaseResult(any(), any()) }
        verify(exactly = 0) { entityManager.merge(any<CaseEntity>()) }
        verify(exactly = 0) { caseSnapshotAssembler.upsertCase(any(), any()) }
      }
    }
  }
}
