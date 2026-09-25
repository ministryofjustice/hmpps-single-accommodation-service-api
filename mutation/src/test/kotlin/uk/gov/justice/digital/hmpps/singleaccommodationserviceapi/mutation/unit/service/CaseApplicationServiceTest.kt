package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.unit.service

import io.mockk.andThenJust
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.runs
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpServerErrorException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremisesanddelius.ApprovedPremisesAndDeliusCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremisesanddelius.CaseSummaries
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseSummaryName
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper.CaseMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseApplicationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseCreationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseMutationOrchestrationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseSnapshotAssembler
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseToCreate
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CrnToPrisonNumber
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.InvalidCrnsException
import java.time.LocalDate
import java.util.UUID

@ExtendWith(MockKExtension::class)
class CaseApplicationServiceTest {

  @Nested
  inner class CreateCases {
    @MockK
    lateinit var caseRepository: CaseRepository

    @RelaxedMockK
    lateinit var caseOrchestrationService: CaseMutationOrchestrationService

    @InjectMockKs
    lateinit var caseApplicationService: CaseApplicationService

    @MockK
    lateinit var caseCreationService: CaseCreationService

    @RelaxedMockK
    lateinit var caseSnapshotAssembler: CaseSnapshotAssembler

    @MockK
    lateinit var caseMapper: CaseMapper

    @MockK
    lateinit var approvedPremisesAndDeliusCachingService: ApprovedPremisesAndDeliusCachingService

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `createCases() retries multiple times on DataIntegrityViolation exception`(createAsBlankRecord: Boolean) {
      val crnToPrisonNumbers = List(100) {
        CrnToPrisonNumber(crn = UUID.randomUUID().toString(), prisonNumber = UUID.randomUUID().toString())
      }

      every { caseCreationService.saveUnpersistedCasesAsBlankRows(any()) } throws
        DataIntegrityViolationException("duplicate-1") andThenThrows
        DataIntegrityViolationException("duplicate-2") andThenJust runs
      every { caseCreationService.saveUnpersistedCases(any()) } throws
        DataIntegrityViolationException("duplicate-1") andThenThrows
        DataIntegrityViolationException("duplicate-2") andThenJust runs

      caseApplicationService.createCases(crnToPrisonNumbers, createAsBlankRecord = createAsBlankRecord)

      // First and second call throws error, so retry, then 100 crns / 25 batch size = 4 calls == 6 calls in total
      if (createAsBlankRecord) {
        verify(exactly = 6) { caseCreationService.saveUnpersistedCasesAsBlankRows(any()) }
      } else {
        verify(exactly = 6) { caseCreationService.saveUnpersistedCases(any()) }
      }
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `createCases() throws after 3 retries`(createAsBlankRecord: Boolean) {
      val crnToPrisonNumbers = List(100) {
        CrnToPrisonNumber(crn = UUID.randomUUID().toString(), prisonNumber = UUID.randomUUID().toString())
      }

      val result = crnToPrisonNumbers.map { it.crn }

      every { caseRepository.findUnpersistedCrns(any()) } answers { result }
      every { caseCreationService.saveUnpersistedCasesAsBlankRows(any()) } throws
        DataIntegrityViolationException("duplicate-1") andThenThrows
        DataIntegrityViolationException("duplicate-2") andThenThrows
        DataIntegrityViolationException("duplicate-3")
      every { caseCreationService.saveUnpersistedCases(any()) } throws
        DataIntegrityViolationException("duplicate-1") andThenThrows
        DataIntegrityViolationException("duplicate-2") andThenThrows
        DataIntegrityViolationException("duplicate-3")

      assertThrows<DataIntegrityViolationException> {
        caseApplicationService.createCases(crnToPrisonNumbers, createAsBlankRecord = createAsBlankRecord)
      }

      if (createAsBlankRecord) {
        verify(exactly = 3) { caseCreationService.saveUnpersistedCasesAsBlankRows(any()) }
      } else {
        verify(exactly = 3) { caseCreationService.saveUnpersistedCases(any()) }
      }
    }
  }

  @Nested
  inner class CreateValidatedCases {
    @MockK
    lateinit var caseRepository: CaseRepository

    @RelaxedMockK
    lateinit var caseCreationService: CaseCreationService

    @MockK
    lateinit var approvedPremisesAndDeliusCachingService: ApprovedPremisesAndDeliusCachingService

    @InjectMockKs
    lateinit var caseApplicationService: CaseApplicationService

    private val crns = listOf("A111111", "B222222", "C333333")
    private val dateOfBirth = LocalDate.of(1990, 1, 2)

    @Test
    fun `does not query for unpersisted crns or call delius when createCases is used`() {
      caseApplicationService.createCases(crns.map { CrnToPrisonNumber(it, null) }, createAsBlankRecord = true)

      verify(exactly = 0) { caseRepository.findUnpersistedCrns(any()) }
      verify(exactly = 0) { approvedPremisesAndDeliusCachingService.postCaseSummaries(any()) }
      verify(exactly = 1) {
        caseCreationService.saveUnpersistedCasesAsBlankRows(
          listOf(
            CaseToCreate(crn = "A111111", prisonNumber = null),
            CaseToCreate(crn = "B222222", prisonNumber = null),
            CaseToCreate(crn = "C333333", prisonNumber = null),
          ),
        )
      }
    }

    @Test
    fun `does not call delius when every crn is already persisted`() {
      every { caseRepository.findUnpersistedCrns(any()) } returns emptyList()

      caseApplicationService.createValidatedCases(crns)

      verify(exactly = 0) { approvedPremisesAndDeliusCachingService.postCaseSummaries(any()) }
      verify(exactly = 1) {
        caseCreationService.saveUnpersistedCasesAsBlankRows(
          listOf(
            CaseToCreate(crn = "A111111", prisonNumber = null),
            CaseToCreate(crn = "B222222", prisonNumber = null),
            CaseToCreate(crn = "C333333", prisonNumber = null),
          ),
        )
      }
    }

    @Test
    fun `only sends unpersisted crns to delius and enriches them from the case summaries`() {
      every { caseRepository.findUnpersistedCrns(any()) } returns listOf("B222222", "C333333")
      every { approvedPremisesAndDeliusCachingService.postCaseSummaries(any()) } returns CaseSummaries(
        listOf(
          buildCaseSummary(
            crn = "B222222",
            nomsId = "B1234BB",
            name = buildCaseSummaryName(forename = "Joe", surname = "Bloggs"),
            dateOfBirth = dateOfBirth,
          ),
          buildCaseSummary(
            crn = "C333333",
            nomsId = "C1234CC",
            name = buildCaseSummaryName(forename = "Joe", surname = "Bloggs"),
            dateOfBirth = dateOfBirth,
          ),
        ),
      )

      caseApplicationService.createValidatedCases(crns)

      verify(exactly = 1) { caseRepository.findUnpersistedCrns(arrayOf("A111111", "B222222", "C333333")) }
      verify(exactly = 1) { approvedPremisesAndDeliusCachingService.postCaseSummaries(listOf("B222222", "C333333")) }
      verify(exactly = 1) {
        caseCreationService.saveUnpersistedCasesAsBlankRows(
          listOf(
            CaseToCreate(crn = "A111111", prisonNumber = null),
            CaseToCreate(
              crn = "B222222",
              prisonNumber = "B1234BB",
              firstName = "Joe",
              lastName = "Bloggs",
              dateOfBirth = dateOfBirth,
            ),
            CaseToCreate(
              crn = "C333333",
              prisonNumber = "C1234CC",
              firstName = "Joe",
              lastName = "Bloggs",
              dateOfBirth = dateOfBirth,
            ),
          ),
        )
      }
    }

    @Test
    fun `leaves a case unenriched when it is already persisted`() {
      every { caseRepository.findUnpersistedCrns(any()) } returns listOf("C333333")
      every { approvedPremisesAndDeliusCachingService.postCaseSummaries(any()) } returns
        CaseSummaries(
          listOf(
            buildCaseSummary(
              crn = "C333333",
              nomsId = "C1234CC",
              name = buildCaseSummaryName(forename = "Joe", surname = "Bloggs"),
              dateOfBirth = dateOfBirth,
            ),
          ),
        )

      caseApplicationService.createValidatedCases(crns)

      verify(exactly = 1) {
        caseCreationService.saveUnpersistedCasesAsBlankRows(
          listOf(
            CaseToCreate(crn = "A111111", prisonNumber = null),
            CaseToCreate(crn = "B222222", prisonNumber = null),
            CaseToCreate(
              crn = "C333333",
              prisonNumber = "C1234CC",
              firstName = "Joe",
              lastName = "Bloggs",
              dateOfBirth = dateOfBirth,
            ),
          ),
        )
      }
    }

    @Test
    fun `collapses duplicate crns before validating and creating`() {
      every { caseRepository.findUnpersistedCrns(any()) } returns listOf("A111111")
      every { approvedPremisesAndDeliusCachingService.postCaseSummaries(any()) } returns
        CaseSummaries(
          listOf(
            buildCaseSummary(
              crn = "A111111",
              nomsId = "A1234AA",
              name = buildCaseSummaryName(forename = "Joe", surname = "Bloggs"),
              dateOfBirth = dateOfBirth,
            ),
          ),
        )

      caseApplicationService.createValidatedCases(listOf("A111111", "A111111"))

      verify(exactly = 1) { approvedPremisesAndDeliusCachingService.postCaseSummaries(listOf("A111111")) }
      verify(exactly = 1) {
        caseCreationService.saveUnpersistedCasesAsBlankRows(
          listOf(
            CaseToCreate(
              crn = "A111111",
              prisonNumber = "A1234AA",
              firstName = "Joe",
              lastName = "Bloggs",
              dateOfBirth = dateOfBirth,
            ),
          ),
        )
      }
    }

    @Test
    fun `throws InvalidCrnsException and creates nothing when delius drops a crn`() {
      every { caseRepository.findUnpersistedCrns(any()) } returns listOf("B222222", "C333333")
      every { approvedPremisesAndDeliusCachingService.postCaseSummaries(any()) } returns
        CaseSummaries(listOf(buildCaseSummary(crn = "B222222")))

      val exception = assertThrows<InvalidCrnsException> {
        caseApplicationService.createValidatedCases(crns)
      }

      assertThat(exception.message).isEqualTo("invalidCrns: C333333")
      verify(exactly = 0) { caseCreationService.saveUnpersistedCasesAsBlankRows(any()) }
      verify(exactly = 0) { caseCreationService.saveUnpersistedCases(any()) }
    }

    @Test
    fun `does not create any blank case rows when delius is unavailable to validate crns`() {
      every { caseRepository.findUnpersistedCrns(any()) } returns listOf("B222222", "C333333")
      every { approvedPremisesAndDeliusCachingService.postCaseSummaries(any()) } throws
        HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR)

      assertThrows<HttpServerErrorException> {
        caseApplicationService.createValidatedCases(crns)
      }

      verify(exactly = 0) { caseCreationService.saveUnpersistedCasesAsBlankRows(any()) }
      verify(exactly = 0) { caseCreationService.saveUnpersistedCases(any()) }
    }

    @Test
    fun `lists every invalid crn in the exception`() {
      every { caseRepository.findUnpersistedCrns(any()) } returns listOf("A111111", "B222222", "C333333")
      every { approvedPremisesAndDeliusCachingService.postCaseSummaries(any()) } returns CaseSummaries(emptyList())

      val exception = assertThrows<InvalidCrnsException> {
        caseApplicationService.createValidatedCases(crns)
      }

      assertThat(exception.message).isEqualTo("invalidCrns: A111111, B222222, C333333")
    }
  }
}
