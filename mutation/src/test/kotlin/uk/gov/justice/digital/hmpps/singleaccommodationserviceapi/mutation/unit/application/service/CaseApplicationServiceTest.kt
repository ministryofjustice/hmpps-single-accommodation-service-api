package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.unit.application.service

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecordCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildIdentifiers
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildTier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseApplicationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseMutationOrchestrationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.factories.buildCaseMutationOrchestrationDto

@ExtendWith(MockKExtension::class)
class CaseApplicationServiceTest {
  @MockK
  lateinit var caseOrchestrationService: CaseMutationOrchestrationService

  @MockK
  lateinit var caseRepository: CaseRepository

  @MockK
  lateinit var corePersonRecordCachingService: CorePersonRecordCachingService

  @InjectMockKs
  lateinit var caseApplicationService: CaseApplicationService

  private val crnOne = "X12345"
  private val crnTwo = "X12346"

  @Nested
  inner class UpsertCases {

    @Test
    fun `should upsert all cases as all cases as all are new`() {
      val crnList = listOf(crnOne, crnTwo)

      val orch1 = buildCaseMutationOrchestrationDto(
        crn = crnOne,
        cpr = buildCorePersonRecord(identifiers = buildIdentifiers(crns = listOf(crnOne))),
        tier = buildTier(TierScore.A1),
        cas1Application = null,
      )

      val orch2 = buildCaseMutationOrchestrationDto(
        crn = crnTwo,
        cpr = buildCorePersonRecord(identifiers = buildIdentifiers(crns = listOf(crnTwo))),
        tier = buildTier(TierScore.B1),
        cas1Application = buildCas1Application(),
      )

      val orchestrationDtoList = listOf(orch1, orch2)

      every {
        caseRepository.findMissingCrns(
          crns = crnList.toTypedArray(),
        )
      } returns crnList

      every { caseOrchestrationService.getCases(crnList) } returns orchestrationDtoList

      val slot = slot<CaseEntity>()

      every { caseRepository.save(capture(slot)) } answers { slot.captured }

      caseApplicationService.upsertCases(crnList)

      verify(exactly = 1) { caseOrchestrationService.getCases(crnList) }
      verify(exactly = 1) { caseRepository.findMissingCrns(crns = crnList.toTypedArray()) }
      verify(exactly = 2) { caseRepository.save(any()) }
    }

    @Test
    fun `should upsert no cases as all cases as none are new`() {
      val crnList = listOf(crnOne, crnTwo)

      every {
        caseRepository.findMissingCrns(
          crns = crnList.toTypedArray(),
        )
      } returns emptyList()

      caseApplicationService.upsertCases(crnList)
      verify(exactly = 0) { caseRepository.save(any()) }
    }
  }
}
