package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.unit.application.service

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationsasdelius.CaseList
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildTier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildUserEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.UserService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseApplicationOrchestrationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseApplicationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.factories.buildCaseApplicationOrchestrationDto

@ExtendWith(MockKExtension::class)
class CaseApplicationServiceTest {
  @MockK
  lateinit var caseApplicationOrchestrationService: CaseApplicationOrchestrationService

  @MockK
  lateinit var userService: UserService

  @MockK
  lateinit var caseRepository: CaseRepository

  @InjectMockKs
  lateinit var caseApplicationService: CaseApplicationService

  private val crnOne = "X12345"
  private val crnTwo = "X12346"
  private val username = "user1"

  @Nested
  inner class UpsertCases {

    @Test
    fun `should upsert all cases as all cases as all are new`() {
      val crnList = listOf(crnOne, crnTwo)

      val caseListItem1 = buildCase(crn = crnOne)
      val caseListItem2 = buildCase(crn = crnTwo)
      val caseList = CaseList(
        cases = listOf(caseListItem1, caseListItem2),
      )

      val orch1 = buildCaseApplicationOrchestrationDto(
        crn = crnOne,
        tier = buildTier(TierScore.A1),
        cas1Application = null,
      )

      val orch2 = buildCaseApplicationOrchestrationDto(
        crn = crnTwo,
        tier = buildTier(TierScore.B1),
        cas1Application = buildCas1Application(),
      )

      val orchestrationDtoList = listOf(orch1, orch2)

      every { userService.authorizeAndRetrieveUser() } returns buildUserEntity(username = username)
      every { caseApplicationOrchestrationService.getCaseList(username) } returns caseList
      every { caseRepository.findByCrns(crnList) } returns emptyList()
      every { caseApplicationOrchestrationService.getFreshCases(crnList) } returns orchestrationDtoList

      val slot = slot<CaseEntity>()

      every { caseRepository.save(capture(slot)) } answers { slot.captured }

      val result = caseApplicationService.upsertCases()
      assertThat(result).hasSize(2)

      assertThat(result.first().crn).isEqualTo(crnOne)
      assertThat(result.first().name).isEqualTo(caseListItem1.name)
      assertThat(result.first().nomsNumber).isEqualTo(caseListItem1.nomsNumber)
      assertThat(result.first().pncNumber).isEqualTo(caseListItem1.pncNumber)
      assertThat(result.first().dateOfBirth).isEqualTo(caseListItem1.dateOfBirth)
      assertThat(result.first().staff).isEqualTo(caseListItem1.staff)
      assertThat(result.first().team).isEqualTo(caseListItem1.team)
      assertThat(result.first().gender).isEqualTo(caseListItem1.gender)
      assertThat(result.first().roshLevel).isEqualTo(caseListItem1.roshLevel)
      assertThat(result.first().expectedReleaseDate).isEqualTo(caseListItem1.expectedReleaseDate)
      assertThat(result.first().userExcluded).isEqualTo(caseListItem1.userExcluded)
      assertThat(result.first().userRestricted).isEqualTo(caseListItem1.userRestricted)
      assertThat(result.first().exclusionMessage).isEqualTo(caseListItem1.exclusionMessage)
      assertThat(result.first().restrictionMessage).isEqualTo(caseListItem1.restrictionMessage)

      assertThat(result.last().crn).isEqualTo(crnTwo)
      assertThat(result.last().name).isEqualTo(caseListItem2.name)
      assertThat(result.last().nomsNumber).isEqualTo(caseListItem2.nomsNumber)
      assertThat(result.last().pncNumber).isEqualTo(caseListItem2.pncNumber)
      assertThat(result.last().dateOfBirth).isEqualTo(caseListItem2.dateOfBirth)
      assertThat(result.last().staff).isEqualTo(caseListItem2.staff)
      assertThat(result.last().team).isEqualTo(caseListItem2.team)
      assertThat(result.last().gender).isEqualTo(caseListItem2.gender)
      assertThat(result.last().roshLevel).isEqualTo(caseListItem2.roshLevel)
      assertThat(result.last().expectedReleaseDate).isEqualTo(caseListItem2.expectedReleaseDate)
      assertThat(result.last().userExcluded).isEqualTo(caseListItem2.userExcluded)
      assertThat(result.last().userRestricted).isEqualTo(caseListItem2.userRestricted)
      assertThat(result.last().exclusionMessage).isEqualTo(caseListItem2.exclusionMessage)
      assertThat(result.last().restrictionMessage).isEqualTo(caseListItem2.restrictionMessage)
    }

    @Test
    fun `should upsert no cases as all cases as none are new`() {
      val crnList = listOf(crnOne, crnTwo)

      val caseListItem1 = buildCase(crn = crnOne)
      val caseListItem2 = buildCase(crn = crnTwo)
      val caseList = CaseList(
        cases = listOf(caseListItem1, caseListItem2),
      )

      val cas1Application = buildCas1Application()

      val caseEntity1 = buildCaseEntity(
        crn = crnOne,
        tier = TierScore.A1,
      )
      val caseEntity2 = buildCaseEntity(
        crn = crnTwo,
        tier = TierScore.B1,
        cas1ApplicationId = cas1Application.id,
        cas1ApplicationApplicationStatus = cas1Application.applicationStatus,
        cas1ApplicationRequestForPlacementStatus = cas1Application.requestForPlacementStatus,
        cas1ApplicationPlacementStatus = cas1Application.placementStatus,
      )
      val caseEntityList = listOf(caseEntity1, caseEntity2)

      every { userService.authorizeAndRetrieveUser() } returns buildUserEntity(username = username)
      every { caseApplicationOrchestrationService.getCaseList(username) } returns caseList
      every { caseRepository.findByCrns(crnList) } returns caseEntityList

      val result = caseApplicationService.upsertCases()
      assertThat(result).hasSize(2)

      assertThat(result.first().crn).isEqualTo(crnOne)
      assertThat(result.first().name).isEqualTo(caseListItem1.name)
      assertThat(result.first().nomsNumber).isEqualTo(caseListItem1.nomsNumber)
      assertThat(result.first().pncNumber).isEqualTo(caseListItem1.pncNumber)
      assertThat(result.first().dateOfBirth).isEqualTo(caseListItem1.dateOfBirth)
      assertThat(result.first().staff).isEqualTo(caseListItem1.staff)
      assertThat(result.first().team).isEqualTo(caseListItem1.team)
      assertThat(result.first().gender).isEqualTo(caseListItem1.gender)
      assertThat(result.first().roshLevel).isEqualTo(caseListItem1.roshLevel)
      assertThat(result.first().expectedReleaseDate).isEqualTo(caseListItem1.expectedReleaseDate)
      assertThat(result.first().userExcluded).isEqualTo(caseListItem1.userExcluded)
      assertThat(result.first().userRestricted).isEqualTo(caseListItem1.userRestricted)
      assertThat(result.first().exclusionMessage).isEqualTo(caseListItem1.exclusionMessage)
      assertThat(result.first().restrictionMessage).isEqualTo(caseListItem1.restrictionMessage)

      assertThat(result.last().crn).isEqualTo(crnTwo)
      assertThat(result.last().name).isEqualTo(caseListItem2.name)
      assertThat(result.last().nomsNumber).isEqualTo(caseListItem2.nomsNumber)
      assertThat(result.last().pncNumber).isEqualTo(caseListItem2.pncNumber)
      assertThat(result.last().dateOfBirth).isEqualTo(caseListItem2.dateOfBirth)
      assertThat(result.last().staff).isEqualTo(caseListItem2.staff)
      assertThat(result.last().team).isEqualTo(caseListItem2.team)
      assertThat(result.last().gender).isEqualTo(caseListItem2.gender)
      assertThat(result.last().roshLevel).isEqualTo(caseListItem2.roshLevel)
      assertThat(result.last().expectedReleaseDate).isEqualTo(caseListItem2.expectedReleaseDate)
      assertThat(result.last().userExcluded).isEqualTo(caseListItem2.userExcluded)
      assertThat(result.last().userRestricted).isEqualTo(caseListItem2.userRestricted)
      assertThat(result.last().exclusionMessage).isEqualTo(caseListItem2.exclusionMessage)
      assertThat(result.last().restrictionMessage).isEqualTo(caseListItem2.restrictionMessage)
    }
  }
}
