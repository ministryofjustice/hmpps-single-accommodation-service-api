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
import org.springframework.dao.DataIntegrityViolationException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseApplicationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseCreationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseMutationOrchestrationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CrnToPrisonNumber
import java.util.UUID

@ExtendWith(MockKExtension::class)
class CaseApplicationServiceTest {

  @Nested
  inner class CaseApplicationServiceTest {
    @MockK
    lateinit var caseRepository: CaseRepository

    @RelaxedMockK
    lateinit var caseOrchestrationService: CaseMutationOrchestrationService

    @InjectMockKs
    lateinit var caseApplicationService: CaseApplicationService

    @MockK
    lateinit var caseCreationService: CaseCreationService

    @Test
    fun `createCases() retries multiple times on DataIntegrityViolation exception`() {
      val crnToPrisonNumbers = List(100) {
        CrnToPrisonNumber(crn = UUID.randomUUID().toString(), prisonNumber = UUID.randomUUID().toString())
      }

      every { caseCreationService.saveUnpersistedCases(any()) } throws
        DataIntegrityViolationException("duplicate-1") andThenThrows
        DataIntegrityViolationException("duplicate-2") andThenJust runs

      caseApplicationService.createCases(crnToPrisonNumbers)

      // First and second call throws error, so retry, then 100 crns / 25 batch size = 4 calls == 6 calls in total
      verify(exactly = 6) { caseCreationService.saveUnpersistedCases(any()) }
    }

    @Test
    fun `createCases() throws after 3 retries`() {
      val crnToPrisonNumbers = List(100) {
        CrnToPrisonNumber(crn = UUID.randomUUID().toString(), prisonNumber = UUID.randomUUID().toString())
      }

      val result = crnToPrisonNumbers.map { it.crn }

      every { caseRepository.findUnpersistedCrns(any()) } answers { result }
      every { caseCreationService.saveUnpersistedCases(any()) } throws
        DataIntegrityViolationException("duplicate-1") andThenThrows
        DataIntegrityViolationException("duplicate-2") andThenThrows
        DataIntegrityViolationException("duplicate-3")

      assertThrows<DataIntegrityViolationException> {
        caseApplicationService.createCases(crnToPrisonNumbers)
      }

      verify(exactly = 3) { caseCreationService.saveUnpersistedCases(any()) }
    }
  }

  @Nested
  inner class CaseCreationServiceTest {

    @MockK
    lateinit var caseRepository: CaseRepository

    @InjectMockKs
    lateinit var caseCreationService: CaseCreationService

    @Test
    fun `saveUnpersistedCases persists only unpersisted Crns`() {
      val first = CrnToPrisonNumber(crn = UUID.randomUUID().toString(), prisonNumber = UUID.randomUUID().toString())
      val second = CrnToPrisonNumber(crn = UUID.randomUUID().toString(), prisonNumber = UUID.randomUUID().toString())
      val third = CrnToPrisonNumber(crn = UUID.randomUUID().toString(), prisonNumber = UUID.randomUUID().toString())
      val crnToPrisonNumbers = listOf(first, second, third)
      val entityList = mutableListOf<List<CaseEntity>>()

      every { caseRepository.findUnpersistedCrns(any()) } returns listOf(first.crn, third.crn)
      every { caseRepository.saveAll(capture(entityList)) } answers { firstArg() }

      caseCreationService.saveUnpersistedCases(crnToPrisonNumbers)

      verify(exactly = 1) { caseRepository.saveAll(any<List<CaseEntity>>()) }
      val entities = entityList[0]
      assertThat(entities).hasSize(2)
      assertThat(entities.map { it.latestCrn() }).containsExactlyInAnyOrder(first.crn, third.crn)
      assertThat(entities[0].latestCrn()).isEqualTo(first.crn)
      assertThat(entities[1].latestCrn()).isEqualTo(third.crn)
    }

    @Test
    fun `Does not save when no cases to persist`() {
      val crnToPrisonNumbers = List(3) {
        CrnToPrisonNumber(
          crn = UUID.randomUUID().toString(),
          prisonNumber = UUID.randomUUID().toString(),
        )
      }

      every { caseRepository.findUnpersistedCrns(any()) } returns emptyList()

      caseCreationService.saveUnpersistedCases(crnToPrisonNumbers)

      verify(exactly = 1) { caseRepository.findUnpersistedCrns(any()) }
      verify(exactly = 0) { caseRepository.saveAll(any<Iterable<CaseEntity>>()) }
    }
  }
}
