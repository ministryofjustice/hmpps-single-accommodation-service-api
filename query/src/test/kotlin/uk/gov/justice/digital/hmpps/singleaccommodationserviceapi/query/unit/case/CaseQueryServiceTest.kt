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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildCaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.OrchestrationResultDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationsasdelius.CaseList
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildName
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildRosh
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildRoshDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildUserEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withCrn
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.UserService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.CaseOrchestrationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.CaseQueryService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.CaseTransformer
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.RiskLevelTransformer
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildCaseOrchestrationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildEligibilityDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildPersonDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildUpstreamFailure
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.shared.UpstreamFailureTransformer.toFailureIdentifier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationoasys.RiskLevel as RiskLevelInfra

@ExtendWith(MockKExtension::class)
class CaseQueryServiceTest {
  @MockK
  lateinit var caseOrchestrationService: CaseOrchestrationService

  @MockK
  lateinit var userService: UserService

  @MockK
  lateinit var caseRepository: CaseRepository

  @MockK
  lateinit var eligibilityService: EligibilityService

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
      assertThat(firstPerson.name).isEqualTo(case1.name)
      assertThat(firstPerson.nomsNumber).isEqualTo(case1.nomsNumber)
      assertThat(firstPerson.pncNumber).isEqualTo(case1.pncNumber)
      assertThat(firstPerson.dateOfBirth).isEqualTo(case1.dateOfBirth)
      assertThat(firstPerson.staff).isEqualTo(case1.staff)
      assertThat(firstPerson.gender).isEqualTo(case1.gender)
      assertThat(firstPerson.roshLevel).isEqualTo(case1.roshLevel)
      assertThat(firstPerson.expectedReleaseDate).isEqualTo(case1.expectedReleaseDate)

      val lastPerson = result.last()
      assertThat(lastPerson.crn).isEqualTo(crnTwo)
      assertThat(lastPerson.name).isEqualTo(case2.name)
      assertThat(lastPerson.nomsNumber).isEqualTo(case2.nomsNumber)
      assertThat(lastPerson.pncNumber).isEqualTo(case2.pncNumber)
      assertThat(lastPerson.dateOfBirth).isEqualTo(case2.dateOfBirth)
      assertThat(lastPerson.staff).isEqualTo(case2.staff)
      assertThat(lastPerson.gender).isEqualTo(case2.gender)
      assertThat(lastPerson.roshLevel).isEqualTo(case2.roshLevel)
      assertThat(lastPerson.expectedReleaseDate).isEqualTo(case2.expectedReleaseDate)
    }
  }

  @Nested
  inner class GetCases {

    @Test
    fun `should get cases as all cases from case table and populate missing data from personDtos`() {
      val crnList = listOf(crnOne, crnTwo)
      val personDto1 = buildPersonDto(crn = crnOne)
      val personDto2 = buildPersonDto(crn = crnTwo)
      val personDtos = listOf(
        personDto1,
        personDto2,
      )
      val caseEntity1 = buildCaseEntity { withCrn(crnOne) }
      val caseEntity2 = buildCaseEntity { withCrn(crnTwo) }
      val caseEntities = listOf(
        caseEntity1,
        caseEntity2,
      )
      val eligibilityDto1 = buildEligibilityDto(
        crn = crnOne,
      )
      val eligibilityDto2 = buildEligibilityDto(
        crn = crnTwo,
      )

      val caseDto1 = buildCaseDto(crn = crnOne)
      val caseDto2 = buildCaseDto(crn = crnTwo)

      every { caseRepository.findByCrns(crnList) } returns caseEntities
      every { eligibilityService.getEligibility(personDto1, caseEntity1) } returns eligibilityDto1
      every { eligibilityService.getEligibility(personDto2, caseEntity2) } returns eligibilityDto2

      val result = caseQueryService.getCases(personDtos = personDtos)
      assertThat(result).hasSize(2)

      assertThat(result.first()).isEqualTo(
        caseDto1,
      )

      assertThat(result.last()).isEqualTo(
        caseDto2,
      )
    }

    @ParameterizedTest(name = "getCases() should return all cases from db")
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

      val result = caseQueryService.getCasesV2(crnList)
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

      val result = caseQueryService.getCasesV2(crnList)
      assertThat(result.data).hasSize(2)
      assertThat(result.upstreamFailures).hasSize(1)
      assertThat(result.upstreamFailures.first().endpoint).isEqualTo("getTierByCrn")
      assertThat(result.upstreamFailures.first().identifier)
        .isEqualTo(toFailureIdentifier(crnOne))
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
