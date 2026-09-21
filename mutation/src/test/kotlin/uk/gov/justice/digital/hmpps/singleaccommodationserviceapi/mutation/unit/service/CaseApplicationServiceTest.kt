package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.unit.service

import io.mockk.andThenJust
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremisesanddelius.ApprovedPremisesAndDeliusClient
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremisesanddelius.CaseSummaries
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper.CaseMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseApplicationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseCreationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseMutationOrchestrationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseSnapshotAssembler
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CrnToPrisonNumber
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.InvalidCrnsException
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
    lateinit var approvedPremisesAndDeliusClient: ApprovedPremisesAndDeliusClient

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
  inner class CreateCasesWithCrnValidation {
    @MockK
    lateinit var caseRepository: CaseRepository

    @MockK
    lateinit var caseCreationService: CaseCreationService

    @MockK
    lateinit var approvedPremisesAndDeliusClient: ApprovedPremisesAndDeliusClient

    @InjectMockKs
    lateinit var caseApplicationService: CaseApplicationService

    private val crnsToPrisonNumbers = listOf(
      CrnToPrisonNumber(crn = "A111111", prisonNumber = null),
      CrnToPrisonNumber(crn = "B222222", prisonNumber = null),
      CrnToPrisonNumber(crn = "C333333", prisonNumber = null),
    )

    @Test
    fun `does not query for unpersisted crns or call delius when validateCrns is false`() {
      every { caseCreationService.saveUnpersistedCasesAsBlankRows(any()) } just runs

      caseApplicationService.createCases(crnsToPrisonNumbers, createAsBlankRecord = true)

      verify(exactly = 0) { caseRepository.findUnpersistedCrns(any()) }
      verify(exactly = 0) { approvedPremisesAndDeliusClient.postCaseSummaries(any()) }
      verify(exactly = 1) { caseCreationService.saveUnpersistedCasesAsBlankRows(crnsToPrisonNumbers) }
    }

    @Test
    fun `does not call delius when every crn is already persisted`() {
      every { caseRepository.findUnpersistedCrns(any()) } returns emptyList()
      every { caseCreationService.saveUnpersistedCasesAsBlankRows(any()) } just runs

      caseApplicationService.createCases(crnsToPrisonNumbers, createAsBlankRecord = true, validateCrns = true)

      verify(exactly = 0) { approvedPremisesAndDeliusClient.postCaseSummaries(any()) }
      verify(exactly = 1) { caseCreationService.saveUnpersistedCasesAsBlankRows(crnsToPrisonNumbers) }
    }

    @Test
    fun `only sends unpersisted crns to delius and creates cases when they are all valid`() {
      every { caseRepository.findUnpersistedCrns(any()) } returns listOf("B222222", "C333333")
      every { approvedPremisesAndDeliusClient.postCaseSummaries(any()) } returns
        CaseSummaries(listOf(buildCaseSummary(crn = "B222222"), buildCaseSummary(crn = "C333333")))
      every { caseCreationService.saveUnpersistedCasesAsBlankRows(any()) } just runs

      caseApplicationService.createCases(crnsToPrisonNumbers, createAsBlankRecord = true, validateCrns = true)

      verify(exactly = 1) { caseRepository.findUnpersistedCrns(arrayOf("A111111", "B222222", "C333333")) }
      verify(exactly = 1) { approvedPremisesAndDeliusClient.postCaseSummaries(listOf("B222222", "C333333")) }
      verify(exactly = 1) { caseCreationService.saveUnpersistedCasesAsBlankRows(crnsToPrisonNumbers) }
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `throws InvalidCrnsException and creates nothing when delius drops a crn`(createAsBlankRecord: Boolean) {
      every { caseRepository.findUnpersistedCrns(any()) } returns listOf("B222222", "C333333")
      every { approvedPremisesAndDeliusClient.postCaseSummaries(any()) } returns
        CaseSummaries(listOf(buildCaseSummary(crn = "B222222")))

      val exception = assertThrows<InvalidCrnsException> {
        caseApplicationService.createCases(crnsToPrisonNumbers, createAsBlankRecord = createAsBlankRecord, validateCrns = true)
      }

      assertThat(exception.message).isEqualTo("invalidCrns: C333333")
      verify(exactly = 0) { caseCreationService.saveUnpersistedCasesAsBlankRows(any()) }
      verify(exactly = 0) { caseCreationService.saveUnpersistedCases(any()) }
    }

    @Test
    fun `lists every invalid crn in the exception`() {
      every { caseRepository.findUnpersistedCrns(any()) } returns listOf("A111111", "B222222", "C333333")
      every { approvedPremisesAndDeliusClient.postCaseSummaries(any()) } returns CaseSummaries(emptyList())

      val exception = assertThrows<InvalidCrnsException> {
        caseApplicationService.createCases(crnsToPrisonNumbers, createAsBlankRecord = true, validateCrns = true)
      }

      assertThat(exception.message).isEqualTo("invalidCrns: A111111, B222222, C333333")
    }

    @Test
    fun `sends unpersisted crns to delius in batches of 500`() {
      val manyCrns = List(1200) { CrnToPrisonNumber(crn = "A%06d".format(it), prisonNumber = null) }
      every { caseRepository.findUnpersistedCrns(any()) } returns manyCrns.map { it.crn }
      every { approvedPremisesAndDeliusClient.postCaseSummaries(any()) } answers {
        CaseSummaries(firstArg<List<String>>().map { buildCaseSummary(crn = it) })
      }
      every { caseCreationService.saveUnpersistedCasesAsBlankRows(any()) } just runs

      caseApplicationService.createCases(manyCrns, createAsBlankRecord = true, validateCrns = true)

      // 1200 crns / 500 batch size = 3 delius calls
      verify(exactly = 3) { approvedPremisesAndDeliusClient.postCaseSummaries(any()) }
    }
  }
}
