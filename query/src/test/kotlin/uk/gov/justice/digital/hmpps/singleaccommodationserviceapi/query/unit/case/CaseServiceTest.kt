package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.case

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AssignedToDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.EligibilityDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.RiskLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildIndividualName
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildRosh
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildRoshDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.CaseOrchestrationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.CaseService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.CaseTransformer
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.RiskLevelTransformer
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildCaseOrchestrationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationoasys.RiskLevel as RiskLevelInfra

@ExtendWith(MockKExtension::class)
class CaseServiceTest {
  @MockK
  lateinit var caseOrchestrationService: CaseOrchestrationService

  @MockK
  lateinit var caseRepository: CaseRepository

  @MockK
  lateinit var eligibilityService: EligibilityService

  @InjectMockKs
  lateinit var caseService: CaseService

  private val crnOne = "X12345"
  private val crnTwo = "X12346"

  @Nested
  inner class GetCases {
    @Test
    fun `should get all cases`() {
      val crnList = listOf(crnOne, crnTwo)

      val caseListItem1 = buildCase(crn = crnOne, name = buildIndividualName("Anne", "Smith"))
      val caseListItem2 = buildCase(crn = crnTwo, name = buildIndividualName("Bob", "Smith"))
      val caseListItems = listOf(caseListItem1, caseListItem2)

      val caseEntity1 = buildCaseEntity(crn = crnOne, tier = TierScore.A1, cas1ApplicationId = null)
      val caseEntity2 = buildCaseEntity(crn = crnTwo, tier = TierScore.B1)
      val caseEntities = listOf(caseEntity1, caseEntity2)

      val eligibilityDto1 = EligibilityDto(
        crn = crnOne,
        cas1 = ServiceResult(
          serviceStatus = ServiceStatus.NOT_ELIGIBLE,
        ),
        cas2Hdc = ServiceResult(
          serviceStatus = ServiceStatus.NOT_ELIGIBLE,
        ),
        cas2PrisonBail = ServiceResult(
          serviceStatus = ServiceStatus.NOT_ELIGIBLE,
        ),
        cas2CourtBail = ServiceResult(
          serviceStatus = ServiceStatus.NOT_ELIGIBLE,
        ),
        cas3 = ServiceResult(
          serviceStatus = ServiceStatus.NOT_ELIGIBLE,
        ),
        caseActions = emptyList(),
        caseStatus = CaseStatus.NO_ACTION_REQUIRED,
      )

      val eligibilityDto2 = EligibilityDto(
        crn = crnTwo,
        cas1 = ServiceResult(
          serviceStatus = ServiceStatus.NOT_ELIGIBLE,
        ),
        cas2Hdc = ServiceResult(
          serviceStatus = ServiceStatus.NOT_ELIGIBLE,
        ),
        cas2PrisonBail = ServiceResult(
          serviceStatus = ServiceStatus.NOT_ELIGIBLE,
        ),
        cas2CourtBail = ServiceResult(
          serviceStatus = ServiceStatus.NOT_ELIGIBLE,
        ),
        cas3 = ServiceResult(
          serviceStatus = ServiceStatus.NOT_ELIGIBLE,
        ),
        caseActions = emptyList(),
        caseStatus = CaseStatus.NO_ACTION_REQUIRED,
      )

      every { caseRepository.findByCrns(crnList) } returns caseEntities
      every { eligibilityService.getCachedEligibility(caseListItem1, caseEntity1) } returns eligibilityDto1
      every { eligibilityService.getCachedEligibility(caseListItem2, caseEntity2) } returns eligibilityDto2

      val result = caseService.getCases(caseListItems)
      assertThat(result).hasSize(2)

      assertThat(result.first()).isEqualTo(
        CaseDto(
          name = "Anne Smith",
          dateOfBirth = caseListItem1.dateOfBirth,
          crn = crnOne,
          prisonNumber = caseListItem1.nomsNumber,
          photoUrl = null,
          tier = uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.TierScore.A1,
          riskLevel = null,
          pncReference = caseListItem1.pncNumber,
          assignedTo = AssignedToDto(caseListItem1.staff.code, name = "Anne Smith"),
          currentAccommodation = null,
          nextAccommodation = null,
          eligibilityDto = eligibilityDto1,
        ),
      )
      assertThat(result[1]).isEqualTo(
        CaseDto(
          name = "Bob Smith",
          dateOfBirth = caseListItem1.dateOfBirth,
          crn = crnTwo,
          prisonNumber = caseListItem1.nomsNumber,
          photoUrl = null,
          tier = uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.TierScore.A1,
          riskLevel = null,
          pncReference = caseListItem1.pncNumber,
          assignedTo = AssignedToDto(caseListItem1.staff.code, name = "Anne Smith"),
          currentAccommodation = null,
          nextAccommodation = null,
          eligibilityDto = eligibilityDto2,
        ),
      )
    }

    @ParameterizedTest(name = "getCases() should return all cases with risk level = {0}")
    @EnumSource(RiskLevelInfra::class)
    fun `should get cases as all cases meet risk-level requirements`(riskLevelInfra: RiskLevelInfra) {
      val crnList = listOf(crnOne, crnTwo)
      val orchestrationDtoList = listOf(
        buildCaseOrchestrationDto(
          crn = crnOne,
          roshDetails = buildRoshDetails(
            rosh = buildRosh(
              riskChildrenCommunity = riskLevelInfra,
            ),
          ),
        ),
        buildCaseOrchestrationDto(
          crn = crnTwo,
          roshDetails = buildRoshDetails(
            rosh = buildRosh(
              riskChildrenCommunity = riskLevelInfra,
            ),
          ),
        ),
      )

      every { caseOrchestrationService.getCases(crnList) } returns orchestrationDtoList

      val result = caseService.getCases(crnList, RiskLevelTransformer.toRiskLevel(riskLevelInfra))
      assertThat(result).hasSize(2)

      val orchestrationOne = orchestrationDtoList.first()
      val orchestrationTwo = orchestrationDtoList[1]
      assertThat(result.first()).isEqualTo(
        CaseTransformer.toCaseDto(
          crn = crnOne,
          cpr = orchestrationOne.cpr,
          roshDetails = orchestrationOne.roshDetails,
          tier = orchestrationOne.tier,
          caseSummaries = orchestrationOne.cases,
        ),
      )
      assertThat(result[1]).isEqualTo(
        CaseTransformer.toCaseDto(
          crn = crnTwo,
          cpr = orchestrationTwo.cpr,
          roshDetails = orchestrationTwo.roshDetails,
          tier = orchestrationTwo.tier,
          caseSummaries = orchestrationTwo.cases,
        ),
      )
    }
  }

  @ParameterizedTest(name = "getCases() should return 0 cases with risk level = {0}")
  @EnumSource(
    value = RiskLevel::class,
    mode = EnumSource.Mode.EXCLUDE,
    names = ["VERY_HIGH"],
  )
  fun `should get empty cases as cases do not meet risk level requirements`(riskLevel: RiskLevel) {
    val crnList = listOf(crnOne, crnTwo)
    val orchestrationDtoList = listOf(
      buildCaseOrchestrationDto(
        crn = crnOne,
        roshDetails = buildRoshDetails(
          rosh = buildRosh(
            riskChildrenCommunity = RiskLevelInfra.VERY_HIGH,
          ),
        ),
      ),
      buildCaseOrchestrationDto(
        crn = crnTwo,
        roshDetails = buildRoshDetails(
          rosh = buildRosh(
            riskChildrenCommunity = RiskLevelInfra.VERY_HIGH,
          ),
        ),
      ),
    )

    every { caseOrchestrationService.getCases(crnList) } returns orchestrationDtoList

    assertThat(caseService.getCases(crnList, riskLevel)).isEmpty()
  }

  @Test
  fun `should return first case but not second as second does not meet risk-level requirements`() {
    val crnList = listOf(crnOne, crnTwo)
    val orchestrationDtoList = listOf(
      buildCaseOrchestrationDto(
        crn = crnOne,
        roshDetails = buildRoshDetails(
          rosh = buildRosh(
            riskChildrenCommunity = RiskLevelInfra.VERY_HIGH,
          ),
        ),
      ),
      buildCaseOrchestrationDto(
        crn = crnTwo,
        roshDetails = buildRoshDetails(
          rosh = buildRosh(
            riskChildrenCommunity = RiskLevelInfra.HIGH,
          ),
        ),
      ),
    )

    every { caseOrchestrationService.getCases(crnList) } returns orchestrationDtoList

    val result = caseService.getCases(crnList, RiskLevel.VERY_HIGH)
    assertThat(result).hasSize(1)

    val orchestrationOne = orchestrationDtoList.first()
    assertThat(result.first()).isEqualTo(
      CaseTransformer.toCaseDto(
        crn = crnOne,
        cpr = orchestrationOne.cpr,
        roshDetails = orchestrationOne.roshDetails,
        tier = orchestrationOne.tier,
        caseSummaries = orchestrationOne.cases,
      ),
    )
  }

  @Nested
  inner class GetCase {
    @Test
    fun `show get case`() {
      val caseOrchestrationDto = buildCaseOrchestrationDto(crn = crnOne)
      every { caseOrchestrationService.getCase(crnOne) } returns caseOrchestrationDto

      assertThat(caseService.getCase(crnOne)).isEqualTo(
        CaseTransformer.toCaseDto(
          crn = crnOne,
          cpr = caseOrchestrationDto.cpr,
          roshDetails = caseOrchestrationDto.roshDetails,
          tier = caseOrchestrationDto.tier,
          caseSummaries = caseOrchestrationDto.cases,
        ),
      )
    }
  }
}
