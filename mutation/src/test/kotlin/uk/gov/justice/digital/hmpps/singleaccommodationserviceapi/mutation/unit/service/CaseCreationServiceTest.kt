package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.unit.service

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseCreationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CrnToPrisonNumber
import java.util.UUID

@ExtendWith(MockKExtension::class)
class CaseCreationServiceTest {

  @Nested
  inner class SaveUnpersistedCases {

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

      caseCreationService.saveUnpersistedCases(crnToPrisonNumbers)

      verify(exactly = 1) { caseRepository.findUnpersistedCrns(any()) }
      verify(exactly = 0) { caseRepository.saveAll(any<Iterable<CaseEntity>>()) }
    }
  }
}
