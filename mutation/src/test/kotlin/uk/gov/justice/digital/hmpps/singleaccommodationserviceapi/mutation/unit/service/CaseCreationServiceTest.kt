package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.unit.service

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import io.mockk.verifyOrder
import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.ErrorDetail
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.FailureType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.OrchestrationResultDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.UpstreamFailure
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_TIER_V3
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper.CaseMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseCreationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseMutationOrchestrationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseMutationOrchestrationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseRefreshRequestService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseSnapshotAssembler
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseToCreate
import java.time.LocalDate
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

    @MockK(relaxed = true)
    lateinit var caseRefreshRequestService: CaseRefreshRequestService

    private val caseMapper = CaseMapper()

    private val tierFailure = UpstreamFailure(
      callKey = GET_TIER_V3,
      type = FailureType.UPSTREAM_HTTP_ERROR,
      errorDetail = ErrorDetail(httpStatus = HttpStatus.INTERNAL_SERVER_ERROR, message = "Internal Server Error"),
    )

    private lateinit var caseCreationService: CaseCreationService

    @BeforeEach
    fun setUp() {
      caseCreationService = CaseCreationService(
        caseOrchestrationService = caseOrchestrationService,
        caseSnapshotAssembler = caseSnapshotAssembler,
        caseRepository = caseRepository,
        caseMapper = caseMapper,
        entityManager = entityManager,
        caseRefreshRequestService = caseRefreshRequestService,
      )
    }

    private fun stubOrchestrationResult(crn: String, upstreamFailures: List<UpstreamFailure> = emptyList()) {
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
          upstreamFailures = upstreamFailures,
        )
    }

    @Nested
    inner class SaveUnpersistedCasesAsBlankRows {
      @Test
      fun `persists only unpersisted Crns`() {
        val first = CaseToCreate(crn = UUID.randomUUID().toString(), prisonNumber = UUID.randomUUID().toString())
        val second = CaseToCreate(crn = UUID.randomUUID().toString(), prisonNumber = UUID.randomUUID().toString())
        val third = CaseToCreate(crn = UUID.randomUUID().toString(), prisonNumber = UUID.randomUUID().toString())
        val casesToCreate = listOf(first, second, third)
        val entities = mutableListOf<CaseEntity>()

        every { caseRepository.findUnpersistedCrns(any()) } returns listOf(first.crn, third.crn)
        every { entityManager.persist(capture(entities)) } just runs

        caseCreationService.saveUnpersistedCasesAsBlankRows(casesToCreate)

        assertThat(entities).hasSize(2)
        assertThat(entities.map { it.latestCrn() }).containsExactly(first.crn, third.crn)
      }

      @Test
      fun `does not save when no cases to persist`() {
        val casesToCreate = List(3) {
          CaseToCreate(
            crn = UUID.randomUUID().toString(),
            prisonNumber = UUID.randomUUID().toString(),
          )
        }

        every { caseRepository.findUnpersistedCrns(any()) } returns emptyList()

        caseCreationService.saveUnpersistedCasesAsBlankRows(casesToCreate)

        verify(exactly = 1) { caseRepository.findUnpersistedCrns(any()) }
        verify(exactly = 0) { entityManager.persist(any()) }
      }

      @Test
      fun `persists the enriched data when the case has been validated`() {
        val caseToCreate = CaseToCreate(
          crn = "A111111",
          prisonNumber = "A1234BC",
          firstName = "Joe",
          lastName = "Bloggs",
          dateOfBirth = LocalDate.of(2000, 12, 3),
        )
        val entities = mutableListOf<CaseEntity>()

        every { caseRepository.findUnpersistedCrns(any()) } returns listOf(caseToCreate.crn)
        every { entityManager.persist(capture(entities)) } just runs

        caseCreationService.saveUnpersistedCasesAsBlankRows(listOf(caseToCreate))

        val entity = entities.single()
        assertThat(entity.firstName).isEqualTo("Joe")
        assertThat(entity.lastName).isEqualTo("Bloggs")
        assertThat(entity.dateOfBirth).isEqualTo(LocalDate.of(2000, 12, 3))
        assertThat(entity.latestCrn()).isEqualTo("A111111")
        assertThat(entity.latestPrisonNumber()).isEqualTo("A1234BC")
      }

      @Test
      fun `persists a blank row when the case has not been enriched`() {
        val caseToCreate = CaseToCreate(crn = "A111111", prisonNumber = "A1234BC")
        val entities = mutableListOf<CaseEntity>()

        every { caseRepository.findUnpersistedCrns(any()) } returns listOf(caseToCreate.crn)
        every { entityManager.persist(capture(entities)) } just runs

        caseCreationService.saveUnpersistedCasesAsBlankRows(listOf(caseToCreate))

        val entity = entities.single()
        assertThat(entity.firstName).isNull()
        assertThat(entity.lastName).isNull()
        assertThat(entity.dateOfBirth).isNull()
        assertThat(entity.latestCrn()).isEqualTo("A111111")
        assertThat(entity.latestPrisonNumber()).isEqualTo("A1234BC")
      }
    }

    @Nested
    inner class SaveUnpersistedCases {

      private val saved = mutableListOf<CaseEntity>()

      @BeforeEach
      fun stubSave() {
        saved.clear()
        every { caseRepository.save(capture(saved)) } answers { firstArg() }
      }

      @Test
      fun `persists only unpersisted Crns and populates all data on the record`() {
        val first = CaseToCreate(crn = "A111111", prisonNumber = "A1234BC")
        val second = CaseToCreate(crn = "B222222", prisonNumber = "B1234BC")
        val third = CaseToCreate(crn = "C333333", prisonNumber = "C1234BC")

        every { caseRepository.findUnpersistedCrns(any()) } returns listOf(first.crn, third.crn)
        stubOrchestrationResult(first.crn)
        stubOrchestrationResult(third.crn)

        caseCreationService.saveUnpersistedCases(listOf(first, second, third))

        val entities = saved.toList()
        assertThat(entities.map { it.latestCrn() }).containsExactly(first.crn, third.crn)
        assertThat(entities.map { it.latestPrisonNumber() }).containsExactly(first.prisonNumber, third.prisonNumber)
        verify(exactly = 2) { caseRepository.save(any()) }
        verify(exactly = 0) { caseOrchestrationService.getCurrentCaseResult(crn = second.crn, prisonNumber = any()) }
      }

      @Test
      fun `does not save when no cases to persist`() {
        val casesToCreate = List(3) {
          CaseToCreate(
            crn = UUID.randomUUID().toString(),
            prisonNumber = UUID.randomUUID().toString(),
          )
        }

        every { caseRepository.findUnpersistedCrns(any()) } returns emptyList()

        caseCreationService.saveUnpersistedCases(casesToCreate)

        verify(exactly = 1) { caseRepository.findUnpersistedCrns(any()) }
        verify(exactly = 0) { caseOrchestrationService.getCurrentCaseResult(any(), any()) }
        verify(exactly = 0) { caseRepository.save(any()) }
        verify(exactly = 0) { caseSnapshotAssembler.upsertCase(any(), any()) }
        verify(exactly = 0) { caseRefreshRequestService.requestLiveRefresh(any()) }
      }

      @Test
      fun `saves the partial data and requests a refresh only for cases with upstream failures`() {
        val failing = CaseToCreate(crn = "A111111", prisonNumber = "A1234BC")
        val healthy = CaseToCreate(crn = "B222222", prisonNumber = "B1234BC")

        every { caseRepository.findUnpersistedCrns(any()) } returns listOf(failing.crn, healthy.crn)
        stubOrchestrationResult(failing.crn, upstreamFailures = listOf(tierFailure))
        stubOrchestrationResult(healthy.crn)

        caseCreationService.saveUnpersistedCases(listOf(failing, healthy))

        val entities = saved.toList()
        assertThat(entities).hasSize(2)
        val failingEntity = entities.single { it.latestCrn() == failing.crn }
        verify(exactly = 1) { caseRefreshRequestService.requestLiveRefresh(failingEntity.id) }
        verify(exactly = 1) { caseRefreshRequestService.requestLiveRefresh(any()) }
      }

      @Test
      fun `does not request a refresh when there are no upstream failures`() {
        val caseToCreate = CaseToCreate(crn = "A111111", prisonNumber = "A1234BC")

        every { caseRepository.findUnpersistedCrns(any()) } returns listOf(caseToCreate.crn)
        stubOrchestrationResult(caseToCreate.crn)

        caseCreationService.saveUnpersistedCases(listOf(caseToCreate))

        verify(exactly = 1) { caseRepository.save(any()) }
        verify(exactly = 0) { caseRefreshRequestService.requestLiveRefresh(any()) }
      }

      @Test
      fun `requests refreshes after the cases have been saved`() {
        val caseToCreate = CaseToCreate(crn = "A111111", prisonNumber = "A1234BC")

        every { caseRepository.findUnpersistedCrns(any()) } returns listOf(caseToCreate.crn)
        stubOrchestrationResult(caseToCreate.crn, upstreamFailures = listOf(tierFailure))

        caseCreationService.saveUnpersistedCases(listOf(caseToCreate))

        verifyOrder {
          caseRepository.save(any())
          caseRefreshRequestService.requestLiveRefresh(any())
        }
      }
    }
  }
}
