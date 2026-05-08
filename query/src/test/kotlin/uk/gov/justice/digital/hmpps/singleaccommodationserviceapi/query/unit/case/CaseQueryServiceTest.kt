package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.case

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseAccess
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.RiskLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildCaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildDutyToReferDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.OrchestrationResultDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.sasanddelius.CaseList
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildName
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildRoshLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildUserEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withCrn
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.UserService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.CaseOrchestrationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.CaseQueryService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.CaseTransformer
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.FullPersonDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.PersonTransformer.toPersonDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.dutytorefer.DutyToReferQueryService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildCaseOrchestrationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildEligibilityDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildExcludedPersonDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildFullPersonDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildRestrictedPersonDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildUpstreamFailure

@ExtendWith(MockKExtension::class)
class CaseQueryServiceTest {
  @MockK
  lateinit var caseOrchestrationService: CaseOrchestrationService

  @MockK
  lateinit var dutyToReferQueryService: DutyToReferQueryService

  @MockK
  lateinit var userService: UserService

  @MockK
  lateinit var caseRepository: CaseRepository

  @MockK(relaxed = true)
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
      val case1 = buildCase(crn = crnOne, name = buildName("Dave"), nomsNumber = "13234")
      val roshLevel = buildRoshLevel(code = "RMRH", description = "Medium Risk")
      val case2 = buildCase(crn = crnTwo, name = buildName("Bob"), nomsNumber = "12234", roshLevel = roshLevel)

      val caseList = CaseList(cases = listOf(case1, case2))

      every { userService.authorizeAndRetrieveUser() } returns buildUserEntity(username = username)

      every { caseOrchestrationService.getCaseList(username) } returns caseList

      val result = caseQueryService.getCaseList()
      assertThat(result).hasSize(2)

      val firstPerson = result.first() as FullPersonDto
      assertThat(firstPerson.crn).isEqualTo(crnOne)
      assertThat(firstPerson.name).isEqualTo(case1.name.fullName)
      assertThat(firstPerson.nomsNumber).isEqualTo(case1.nomsNumber)
      assertThat(firstPerson.pncNumber).isEqualTo(case1.pncNumber)
      assertThat(firstPerson.dateOfBirth).isEqualTo(case1.dateOfBirth)
      assertThat(firstPerson.assignedTo.forename).isEqualTo(case1.staff.name.forename)
      assertThat(firstPerson.assignedTo.surname).isEqualTo(case1.staff.name.surname)
      assertThat(firstPerson.assignedTo.username).isEqualTo(case1.staff.username)
      assertThat(firstPerson.assignedTo.staffCode).isEqualTo(case1.staff.code)
      assertThat(firstPerson.gender).isEqualTo(case1.gender)
      assertThat(firstPerson.roshLevel).isEqualTo(RiskLevel.VERY_HIGH)

      val lastPerson = result.last() as FullPersonDto
      assertThat(lastPerson.crn).isEqualTo(crnTwo)
      assertThat(lastPerson.name).isEqualTo(case2.name.fullName)
      assertThat(lastPerson.nomsNumber).isEqualTo(case2.nomsNumber)
      assertThat(lastPerson.pncNumber).isEqualTo(case2.pncNumber)
      assertThat(lastPerson.dateOfBirth).isEqualTo(case2.dateOfBirth)
      assertThat(lastPerson.assignedTo.forename).isEqualTo(case1.staff.name.forename)
      assertThat(lastPerson.assignedTo.surname).isEqualTo(case2.staff.name.surname)
      assertThat(lastPerson.assignedTo.username).isEqualTo(case2.staff.username)
      assertThat(lastPerson.assignedTo.staffCode).isEqualTo(case2.staff.code)
      assertThat(lastPerson.gender).isEqualTo(case2.gender)
      assertThat(lastPerson.roshLevel).isEqualTo(RiskLevel.MEDIUM)
    }
  }

  @Nested
  inner class FilteredCaseList {

    @BeforeEach
    fun setup() {
      every { caseRepository.mapByCrns(any()) } returns emptyMap()
      every { eligibilityService.getEligibility(any(), any(), any(), any()) } returns buildEligibilityDto("mock")
    }

    val personDtos = listOf(
      buildFullPersonDto(crn = "CRN1", nomsNumber = "PRI_1", roshLevel = null, teamCode = "TestTeam1"),
      buildFullPersonDto(
        crn = "CRN2",
        nomsNumber = "PRI_2",
        name = buildName(forename = "QQQQQ"),
        roshLevel = RiskLevel.LOW,
        teamCode = "TestTeam2",
      ),
      buildRestrictedPersonDto(
        crn = "CRN3",
        nomsNumber = "PRI_3",
        roshLevel = RiskLevel.MEDIUM,
        teamCode = "TestTeam1",
      ),
      buildRestrictedPersonDto(
        crn = "CRN4",
        nomsNumber = "PRI_4",
        roshLevel = RiskLevel.VERY_HIGH,
        teamCode = "TestTeam2",
      ),
      buildExcludedPersonDto(crn = "CRN5", nomsNumber = "PRI_5", teamCode = "TestTeam1"),
      buildExcludedPersonDto(crn = "CRN6", nomsNumber = "PRI_6", teamCode = "TestTeam3"),
    )

    @Test
    fun `returns all people when no filters provided`() {
      val result = caseQueryService.getCases(personDtos)
      assertThat(result).hasSize(6)
    }

    @ParameterizedTest
    @CsvSource(
      value = [
        "crn1,1,FULL",
        "crn3,1,RESTRICTED",
        "crn5,1,EXCLUDED",
        "null,6,null",
        "'',6,null",
      ],
      nullValues = ["null"],
    )
    fun `filters by CRN search ignoring case, only returns excluded LAO when null or empty`(
      searchTerm: String?,
      count: Int,
      caseAccess: CaseAccess?,
    ) {
      val result = caseQueryService.getCases(personDtos = personDtos, searchTerm = searchTerm)
      assertThat(result).hasSize(count)

      if (count == 1) {
        assertThat(result.first().crn).isEqualToIgnoringCase(searchTerm)
        assertThat(result.first().caseAccess).isEqualTo(caseAccess)
      }
    }

    @Test
    fun `no identifiable information is returned when CaseAccess is EXCLUDED`() {
      val result = caseQueryService.getCases(personDtos = personDtos)
      assertThat(result).hasSize(6)

      val excludedCases = result.filter { it.caseAccess == CaseAccess.EXCLUDED }
      assertThat(excludedCases).allMatch {
        it.name == null &&
          it.dateOfBirth == null &&
          it.pncReference == null &&
          it.riskLevel == null
      }
    }

    @Test
    fun `filters by noms number ignoring case, returns excluded LAO`() {
      val result = caseQueryService.getCases(personDtos = personDtos, searchTerm = "pri_6")
      assertThat(result).hasSize(1).allMatch { it.prisonNumber == "PRI_6" }
    }

    @ParameterizedTest
    @CsvSource(
      value = [
        "xxxxx,0",
        "qqqqq,1",
        "fIrSt,3",
        "'',6",
        "null,6",
      ],
      nullValues = ["null"],
    )
    fun `filters by name ignoring case, does not return excluded LAO when null or empty `(
      searchTerm: String?,
      count: Int,
    ) {
      val result = caseQueryService.getCases(personDtos = personDtos, searchTerm = searchTerm)
      assertThat(result).hasSize(count)
      if (!searchTerm.isNullOrEmpty()) {
        assertThat(result)
          .allMatch { it.name!!.contains(searchTerm, ignoreCase = true) }
          .noneMatch { it.caseAccess == CaseAccess.EXCLUDED }
      }
    }

    @ParameterizedTest
    @CsvSource(
      value = [
        "LOW,1",
        "MEDIUM,1",
        "HIGH,0",
        "VERY_HIGH,1",
        "null,6",
      ],
      nullValues = ["null"],
    )
    fun `filters by risk level`(riskLevel: RiskLevel?, count: Int) {
      val result = caseQueryService.getCases(personDtos = personDtos, riskLevel = riskLevel)
      assertThat(result).hasSize(count)
    }

    @ParameterizedTest
    @CsvSource(
      "TestTeam1, 3",
      "TestTeam2,2",
      "TestTeam3,1",
      "TestTeam4,0",
    )
    fun `filters by team code`(teamCode: String, count: Int) {
      val result = caseQueryService.getCases(personDtos = personDtos, teamCode = teamCode)
      assertThat(result).hasSize(count)
    }

    @Test
    fun `does not call eligibility service for excluded people`() {
      val excluded = listOf(buildExcludedPersonDto(crn = "excluded"))

      every { caseRepository.mapByCrns(any()) } returns emptyMap()

      caseQueryService.getCases(excluded)

      verify(exactly = 0) {
        eligibilityService.getEligibility(any(), any(), any(), any())
      }
    }

    @Test
    fun `calls eligibility service for full person`() {
      val crn = "CRN1"
      val person = buildFullPersonDto(crn = crn)

      every { caseRepository.mapByCrns(any()) } returns mapOf(crn to buildCaseEntity { withCrn(crn) })
      every { eligibilityService.getEligibility(any(), any(), any(), any()) } returns buildEligibilityDto(crn)
      every {
        dutyToReferQueryService.getDutyToRefer(
          any(CaseEntity::class),
          any(String::class),
        )
      } returns buildDutyToReferDto(crn)

      caseQueryService.getCases(listOf(person))

      verify {
        eligibilityService.getEligibility(
          crn = "CRN1",
          gender = person.gender,
          caseEntity = any(),
          dutyToRefer = any(),
        )
      }
    }

    @Test
    fun `calls eligibility service for restricted person`() {
      val crn = "CRN1"
      val person = buildRestrictedPersonDto(crn = crn)

      every { caseRepository.mapByCrns(any()) } returns mapOf(crn to buildCaseEntity { withCrn(crn) })
      every { eligibilityService.getEligibility(any(), any(), any(), any()) } returns buildEligibilityDto(crn)
      every {
        dutyToReferQueryService.getDutyToRefer(
          any(CaseEntity::class),
          any(String::class),
        )
      } returns buildDutyToReferDto(crn)

      caseQueryService.getCases(listOf(person))

      verify {
        eligibilityService.getEligibility(
          crn = "CRN1",
          gender = person.gender,
          caseEntity = any(),
          dutyToRefer = any(),
        )
      }
    }
  }

  @Nested
  inner class GetCases {

    @Test
    fun `should get cases as all cases from case table and populate missing data from personDtos`() {
      val crnList = listOf(crnOne, crnTwo)
      val personDto1 = buildFullPersonDto(crn = crnOne)
      val personDto2 = buildFullPersonDto(crn = crnTwo)
      val personDtos = listOf(
        personDto1,
        personDto2,
      )
      val caseEntity1 = buildCaseEntity { withCrn(crnOne) }
      val caseEntity2 = buildCaseEntity { withCrn(crnTwo) }
      val caseEntities = mapOf(crnOne to caseEntity1, crnTwo to caseEntity2)

      val dutyToReferDto1 = buildDutyToReferDto(crn = crnOne)
      val dutyToReferDto2 = buildDutyToReferDto(crn = crnTwo)

      val eligibilityDto1 = buildEligibilityDto(
        crn = crnOne,
      )
      val eligibilityDto2 = buildEligibilityDto(
        crn = crnTwo,
      )

      val caseDto1 = buildCaseDto(crn = crnOne)
      val caseDto2 = buildCaseDto(crn = crnTwo)

      every { dutyToReferQueryService.getDutyToRefer(caseEntity1, crnOne) } returns dutyToReferDto1
      every { dutyToReferQueryService.getDutyToRefer(caseEntity2, crnTwo) } returns dutyToReferDto2
      every { caseRepository.mapByCrns(crnList) } returns caseEntities
      every {
        eligibilityService.getEligibility(
          personDto1.crn,
          personDto1.gender,
          caseEntity1,
          dutyToReferDto1,
        )
      } returns eligibilityDto1
      every {
        eligibilityService.getEligibility(
          personDto2.crn,
          personDto2.gender,
          caseEntity2,
          dutyToReferDto2,
        )
      } returns eligibilityDto2

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
      every { userService.authorizeAndRetrieveUser() } returns buildUserEntity(username = username)
      val caseOrchestrationDto = buildCaseOrchestrationDto(crn = crnOne)

      every { caseOrchestrationService.getCase(username, crnOne) } returns OrchestrationResultDto(
        data = caseOrchestrationDto,
      )

      val person = toPersonDto(caseOrchestrationDto.case!!)

      val result = caseQueryService.getCase(crnOne)
      assertThat(result.data).isEqualTo(
        CaseTransformer.toCaseDto(
          crn = crnOne,
          person = person,
          cpr = caseOrchestrationDto.cpr,
          roshDetails = caseOrchestrationDto.roshDetails,
          tier = caseOrchestrationDto.tier,
        ),
      )
      assertThat(result.upstreamFailures).isEmpty()
    }

    @Test
    fun `should return case with upstream failures on partial success`() {
      every { userService.authorizeAndRetrieveUser() } returns buildUserEntity(username = username)
      val failures = listOf(
        buildUpstreamFailure(callKey = "getRoshDetail"),
        buildUpstreamFailure(callKey = "getTierByCrn"),
      )
      val caseOrchestrationDto = buildCaseOrchestrationDto(crn = crnOne, roshDetails = null, tier = null)

      every { caseOrchestrationService.getCase(username, crnOne) } returns OrchestrationResultDto(
        data = caseOrchestrationDto,
        upstreamFailures = failures,
      )

      val result = caseQueryService.getCase(crnOne)
      assertThat(result.data.riskLevel).isNull()
      assertThat(result.upstreamFailures).hasSize(2)
    }
  }
}
