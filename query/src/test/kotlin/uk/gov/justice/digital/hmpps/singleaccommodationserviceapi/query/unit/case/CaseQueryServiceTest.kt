package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.case

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildCaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.OrchestrationResultDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationsasdelius.CaseList
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildName
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildUserEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withCrn
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.UserService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.CaseOrchestrationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.CaseQueryService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.CaseTransformer
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildCaseOrchestrationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildEligibilityDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildPersonDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildUpstreamFailure

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

      val lastPerson = result.last()
      assertThat(lastPerson.crn).isEqualTo(crnTwo)
      assertThat(lastPerson.name).isEqualTo(case2.name)
      assertThat(lastPerson.nomsNumber).isEqualTo(case2.nomsNumber)
      assertThat(lastPerson.pncNumber).isEqualTo(case2.pncNumber)
      assertThat(lastPerson.dateOfBirth).isEqualTo(case2.dateOfBirth)
      assertThat(lastPerson.staff).isEqualTo(case2.staff)
      assertThat(lastPerson.gender).isEqualTo(case2.gender)
      assertThat(lastPerson.roshLevel).isEqualTo(case2.roshLevel)
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
  }

  @Nested
  inner class GetCase {

    @Test
    fun `should return case with no upstream failures when all calls succeed`() {
      val caseOrchestrationDto = buildCaseOrchestrationDto(crn = crnOne)

      every { caseOrchestrationService.getCase(crnOne) } returns OrchestrationResultDto(
        data = caseOrchestrationDto,
      )

      val result = caseQueryService.getCase(crnOne)
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

      every { caseOrchestrationService.getCase(crnOne) } returns OrchestrationResultDto(
        data = caseOrchestrationDto,
        upstreamFailures = failures,
      )

      val result = caseQueryService.getCase(crnOne)
      assertThat(result.data.riskLevel).isNull()
      assertThat(result.upstreamFailures).hasSize(2)
    }
  }
}
