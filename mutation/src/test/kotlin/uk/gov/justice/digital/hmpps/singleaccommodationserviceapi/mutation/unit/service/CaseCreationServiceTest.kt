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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.utils.JsonHelper.jsonMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper.CaseMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseCreationService
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

    private val caseMapper = CaseMapper(jsonMapper)
    private lateinit var caseCreationService: CaseCreationService

    @BeforeEach
    fun setUp() {
      caseCreationService = CaseCreationService(caseRepository, caseMapper, entityManager)
    }

    @Test
    fun `saveUnpersistedCases persists only unpersisted Crns`() {
      val first = CrnToPrisonNumber(crn = UUID.randomUUID().toString(), prisonNumber = UUID.randomUUID().toString())
      val second = CrnToPrisonNumber(crn = UUID.randomUUID().toString(), prisonNumber = UUID.randomUUID().toString())
      val third = CrnToPrisonNumber(crn = UUID.randomUUID().toString(), prisonNumber = UUID.randomUUID().toString())
      val crnToPrisonNumbers = listOf(first, second, third)
      val entities = mutableListOf<CaseEntity>()

      every { caseRepository.findUnpersistedCrns(any()) } returns listOf(first.crn, third.crn)
      every { entityManager.persist(capture(entities)) } just runs

      caseCreationService.saveUnpersistedCases(crnToPrisonNumbers)

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
      verify(exactly = 0) { entityManager.persist(any()) }
    }
  }
}
