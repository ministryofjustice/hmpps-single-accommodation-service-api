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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.RiskLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationoasys.RiskLevel as RiskLevelInfra
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildRosh
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildRoshDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.CaseOrchestrationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.CaseQueryService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.toCaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.toRiskLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factory.buildCaseOrchestrationDto
import kotlin.collections.first

@ExtendWith(MockKExtension::class)
class CaseServiceTest {
  @MockK
  lateinit var caseOrchestrationService: CaseOrchestrationService

  @InjectMockKs
  lateinit var caseQueryService: CaseQueryService

  private val crnOne = "X12345"
  private val crnTwo = "X12346"

  @Nested
  inner class GetCases {

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

      val result = caseQueryService.getCases(crnList, toRiskLevel(riskLevelInfra))
      assertThat(result).hasSize(2)

      val orchestrationOne = orchestrationDtoList.first()
      val orchestrationTwo = orchestrationDtoList[1]
        assertThat(result.first()).isEqualTo(
          toCaseDto(
            crn = crnOne,
            cpr = orchestrationOne.cpr,
            roshDetails = orchestrationOne.roshDetails,
            tier = orchestrationOne.tier,
            caseSummaries = orchestrationOne.cases,
          ),
        )
        assertThat(result[1]).isEqualTo(
          toCaseDto(
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

      assertThat(caseQueryService.getCases(crnList, riskLevel)).isEmpty()
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

      val result = caseQueryService.getCases(crnList, RiskLevel.VERY_HIGH)
      assertThat(result).hasSize(1)

      val orchestrationOne = orchestrationDtoList.first()
      assertThat(result.first()).isEqualTo(
        toCaseDto(
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

      assertThat(caseQueryService.getCase(crnOne)).isEqualTo(
        toCaseDto(
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
