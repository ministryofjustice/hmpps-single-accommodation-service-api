package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.unit.case

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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.case.CaseAggregate
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.case.CaseOrchestrationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.case.CaseService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.probationintegrationoasys.RiskLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.factory.buildCaseOrchestrationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.factory.buildRosh
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.factory.buildRoshDetails
import kotlin.collections.first

@ExtendWith(MockKExtension::class)
class CaseServiceTest {
  @MockK
  lateinit var caseOrchestrationService: CaseOrchestrationService

  @InjectMockKs
  lateinit var caseService: CaseService

  private val crnOne = "X12345"
  private val crnTwo = "X12346"

  @Nested
  inner class GetCases {

    @ParameterizedTest(name = "getCases() should return all cases with risk level = {0}")
    @EnumSource(RiskLevel::class)
    fun `should get cases as all cases meet risk-level requirements`(riskLevel: RiskLevel) {
      val crnList = listOf(crnOne, crnTwo)
      val orchestrationDtoList = listOf(
        buildCaseOrchestrationDto(
          crn = crnOne,
          roshDetails = buildRoshDetails(
            rosh = buildRosh(
              riskChildrenCommunity = riskLevel,
            ),
          ),
        ),
        buildCaseOrchestrationDto(
          crn = crnTwo,
          roshDetails = buildRoshDetails(
            rosh = buildRosh(
              riskChildrenCommunity = riskLevel,
            ),
          ),
        ),
      )

      every { caseOrchestrationService.getCases(crnList) } returns orchestrationDtoList

      val result = caseService.getCases(crnList, riskLevel)
      assertThat(result).hasSize(2)

      val orchestrationOne = orchestrationDtoList.first()
      val orchestrationTwo = orchestrationDtoList[1]
      assertThat(result.first()).isEqualTo(
        CaseAggregate.hydrate(orchestrationOne).getCaseDto(),
      )
      assertThat(result[1]).isEqualTo(
        CaseAggregate.hydrate(orchestrationTwo).getCaseDto(),
      )
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
              riskChildrenCommunity = RiskLevel.VERY_HIGH,
            ),
          ),
        ),
        buildCaseOrchestrationDto(
          crn = crnTwo,
          roshDetails = buildRoshDetails(
            rosh = buildRosh(
              riskChildrenCommunity = RiskLevel.VERY_HIGH,
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
              riskChildrenCommunity = RiskLevel.VERY_HIGH,
            ),
          ),
        ),
        buildCaseOrchestrationDto(
          crn = crnTwo,
          roshDetails = buildRoshDetails(
            rosh = buildRosh(
              riskChildrenCommunity = RiskLevel.HIGH,
            ),
          ),
        ),
      )

      every { caseOrchestrationService.getCases(crnList) } returns orchestrationDtoList

      val result = caseService.getCases(crnList, RiskLevel.VERY_HIGH)
      assertThat(result).hasSize(1)

      val orchestrationOne = orchestrationDtoList.first()
      assertThat(result.first()).isEqualTo(
        CaseAggregate.hydrate(orchestrationOne).getCaseDto(),
      )
    }
  }

  @Nested
  inner class GetCase {
    @Test
    fun `show get case`() {
      val caseOrchestrationDto = buildCaseOrchestrationDto(crn = crnOne)
      every { caseOrchestrationService.getCase(crnOne) } returns caseOrchestrationDto

      assertThat(caseService.getCase(crnOne)).isEqualTo(
        CaseAggregate.hydrate(caseOrchestrationDto).getCaseDto(),
      )
    }
  }
}
