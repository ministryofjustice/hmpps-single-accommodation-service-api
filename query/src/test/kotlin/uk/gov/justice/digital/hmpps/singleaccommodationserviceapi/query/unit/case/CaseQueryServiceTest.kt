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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.OrchestrationResultDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationsasdelius.CaseList
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildName
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildRosh
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildRoshDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildUserEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.UserService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.CaseOrchestrationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.CaseQueryService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.CaseTransformer
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.RiskLevelTransformer
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildCaseOrchestrationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildUpstreamFailure
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationoasys.RiskLevel as RiskLevelInfra

@ExtendWith(MockKExtension::class)
class CaseQueryServiceTest {
  @MockK
  lateinit var caseOrchestrationService: CaseOrchestrationService

  @MockK
  lateinit var userService: UserService

  @InjectMockKs
  lateinit var caseQueryService: CaseQueryService

  private val crnOne = "X12345"
  private val crnTwo = "X12346"
  private val username = "user1"

  @Nested
  inner class GetCaseList {

    @Test
    fun `should get case list`() {
      val case1 = buildCase(
        crn = crnOne,
        name = buildName(
          "Dave",
        ),
        nomsNumber = "13234",
      )
      val case2 = buildCase(
        crn = crnTwo,
        name = buildName(
          "Bob",
        ),
        nomsNumber = "12234",
      )

      val caseList = CaseList(
        cases = listOf(
          case1,
          case2,
        ),
      )

      every { userService.authorizeAndRetrieveUser() } returns buildUserEntity(username = username)

      every { caseOrchestrationService.getCaseList(username) } returns caseList

      val result = caseQueryService.getCaseList()
      assertThat(result).hasSize(2)

      val firstPerson = result.first()
      assertThat(firstPerson.crn).isEqualTo(crnOne)
      assertThat(firstPerson.name).isEqualTo("Dave Middle Sur")
      assertThat(firstPerson.nomsNumber).isEqualTo(case1.nomsNumber)
      assertThat(firstPerson.pncNumber).isEqualTo(case1.pncNumber)
      assertThat(firstPerson.dateOfBirth).isEqualTo(case1.dateOfBirth)
      assertThat(firstPerson.staff).isEqualTo(case1.staff)
      assertThat(firstPerson.gender).isEqualTo(case1.gender)
      assertThat(firstPerson.roshLevelCode).isEqualTo(case1.roshLevel?.code)
      assertThat(firstPerson.expectedReleaseDate).isEqualTo(case1.expectedReleaseDate)

      val lastPerson = result.last()
      assertThat(lastPerson.crn).isEqualTo(crnTwo)
      assertThat(lastPerson.name).isEqualTo("Bob Middle Sur")
      assertThat(lastPerson.nomsNumber).isEqualTo(case2.nomsNumber)
      assertThat(lastPerson.pncNumber).isEqualTo(case2.pncNumber)
      assertThat(lastPerson.dateOfBirth).isEqualTo(case2.dateOfBirth)
      assertThat(lastPerson.staff).isEqualTo(case2.staff)
      assertThat(lastPerson.gender).isEqualTo(case2.gender)
      assertThat(lastPerson.roshLevelCode).isEqualTo(case2.roshLevel?.code)
      assertThat(lastPerson.expectedReleaseDate).isEqualTo(case2.expectedReleaseDate)
    }
  }

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

      val result = caseQueryService.getCases(crnList, RiskLevelTransformer.toRiskLevel(riskLevelInfra))
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
        CaseTransformer.toCaseDto(
          crn = crnOne,
          cpr = orchestrationOne.cpr,
          roshDetails = orchestrationOne.roshDetails,
          tier = orchestrationOne.tier,
          caseSummaries = orchestrationOne.cases,
        ),
      )
    }
  }

  @Nested
  inner class GetCase {
    @Test
    fun `show get case`() {
      val caseOrchestrationDto = buildCaseOrchestrationDto(crn = crnOne)
      every { caseOrchestrationService.getCase(crnOne) } returns caseOrchestrationDto

      assertThat(caseQueryService.getCase(crnOne)).isEqualTo(
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

  @Nested
  inner class GetCasesV2 {

    @Test
    fun `should return cases with no upstream failures when all calls succeed`() {
      val crnList = listOf(crnOne, crnTwo)
      val orchestrationDtoList = listOf(
        buildCaseOrchestrationDto(crn = crnOne),
        buildCaseOrchestrationDto(crn = crnTwo),
      )

      every { caseOrchestrationService.getCasesV2(crnList) } returns OrchestrationResultDto(
        data = orchestrationDtoList,
      )

      val result = caseQueryService.getCasesV2(crnList, null)
      assertThat(result.data).hasSize(2)
      assertThat(result.upstreamFailures).isEmpty()
    }

    @Test
    fun `should return cases with upstream failures on partial success`() {
      val crnList = listOf(crnOne, crnTwo)
      val failures = listOf(
        buildUpstreamFailure(callKey = "getTierByCrn", identifier = crnOne),
      )
      val orchestrationDtoList = listOf(
        buildCaseOrchestrationDto(crn = crnOne, tier = null),
        buildCaseOrchestrationDto(crn = crnTwo),
      )

      every { caseOrchestrationService.getCasesV2(crnList) } returns OrchestrationResultDto(
        data = orchestrationDtoList,
        upstreamFailures = failures,
      )

      val result = caseQueryService.getCasesV2(crnList, null)
      assertThat(result.data).hasSize(2)
      assertThat(result.upstreamFailures).hasSize(1)
      assertThat(result.upstreamFailures.first().endpoint).isEqualTo("getTierByCrn")
      assertThat(result.upstreamFailures.first().identifier).isEqualTo(crnOne)
    }

    @Test
    fun `should filter by risk level and still include upstream failures`() {
      val crnList = listOf(crnOne, crnTwo)
      val failures = listOf(
        buildUpstreamFailure(callKey = "getRoshDetail", identifier = crnTwo),
      )
      val orchestrationDtoList = listOf(
        buildCaseOrchestrationDto(
          crn = crnOne,
          roshDetails = buildRoshDetails(rosh = buildRosh(riskChildrenCommunity = RiskLevelInfra.VERY_HIGH)),
        ),
        buildCaseOrchestrationDto(crn = crnTwo, roshDetails = null),
      )

      every { caseOrchestrationService.getCasesV2(crnList) } returns OrchestrationResultDto(
        data = orchestrationDtoList,
        upstreamFailures = failures,
      )

      val result = caseQueryService.getCasesV2(crnList, RiskLevel.VERY_HIGH)
      assertThat(result.data).hasSize(1)
      assertThat(result.data.first().crn).isEqualTo(crnOne)
      assertThat(result.upstreamFailures).hasSize(1)
    }
  }

  @Nested
  inner class GetCaseV2 {

    @Test
    fun `should return case with no upstream failures when all calls succeed`() {
      val caseOrchestrationDto = buildCaseOrchestrationDto(crn = crnOne)

      every { caseOrchestrationService.getCaseV2(crnOne) } returns OrchestrationResultDto(
        data = caseOrchestrationDto,
      )

      val result = caseQueryService.getCaseV2(crnOne)
      assertThat(result.data).isEqualTo(
        CaseTransformer.toCaseDto(
          crn = crnOne,
          cpr = caseOrchestrationDto.cpr,
          roshDetails = caseOrchestrationDto.roshDetails,
          tier = caseOrchestrationDto.tier,
          caseSummaries = caseOrchestrationDto.cases,
        ),
      )
      assertThat(result.upstreamFailures).isEmpty()
    }

    @Test
    fun `should return case with upstream failures on partial success`() {
      val failures = listOf(
        buildUpstreamFailure(callKey = "getRoshDetail"),
        buildUpstreamFailure(callKey = "getTierByCrn"),
      )
      val caseOrchestrationDto = buildCaseOrchestrationDto(crn = crnOne, roshDetails = null, tier = null)

      every { caseOrchestrationService.getCaseV2(crnOne) } returns OrchestrationResultDto(
        data = caseOrchestrationDto,
        upstreamFailures = failures,
      )

      val result = caseQueryService.getCaseV2(crnOne)
      assertThat(result.data.riskLevel).isNull()
      assertThat(result.data.tier).isNull()
      assertThat(result.upstreamFailures).hasSize(2)
    }
  }
}
