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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AssignedToDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.RiskLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.UserAccess
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildCaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildDutyToReferDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.OrchestrationResultDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildName
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildOfficer
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildRoshLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildUserEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withCrn
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.UserService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.Username
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.CaseOrchestrationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.CaseQueryService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.CaseTransformer
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.FullPersonDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.PersonTransformer.toPersonDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.dutytorefer.DutyToReferQueryService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildCaseOrchestrationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildEligibilityDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildFullPersonDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildLimitedPersonDto
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

      val caseList = OrchestrationResultDto(data = listOf(case1, case2))

      every { userService.authorizeAndRetrieveUser() } returns buildUserEntity(username = username)

      every { caseOrchestrationService.getCaseList(username) } returns caseList

      val result = caseQueryService.getCaseList()
      assertThat(result.data).hasSize(2)

      val firstPerson = result.data.first() as FullPersonDto
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

      val lastPerson = result.data.last() as FullPersonDto
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
  inner class CaseListFilters {

    @BeforeEach
    fun setup() {
      every { userService.getUsername() } returns Username(username)
      every { caseRepository.mapByCrns(any()) } returns emptyMap()
      every { eligibilityService.getEligibility(any(), any(), any(), any()) } returns buildEligibilityDto("mock")
    }

    val assignedTo = AssignedToDto(
      forename = "Firstname",
      surname = "Surname",
      username = username,
      staffCode = "5318008",
    )

    val assignedToOther = AssignedToDto(
      forename = "Second",
      surname = "User",
      username = "Second.User",
      staffCode = "12345678",
    )

    val personDtos = listOf(
      buildFullPersonDto(
        crn = "CRN1",
        nomsNumber = "PRI_1",
        name = buildName(surname = "MultiCaseSurname"),
        roshLevel = null,
        teamCode = "TestTeam1",
        assignedTo = assignedTo,
      ),
      buildFullPersonDto(
        crn = "CRN2",
        nomsNumber = "PRI_2",
        name = buildName(forename = "QQQQQ"),
        roshLevel = RiskLevel.LOW,
        teamCode = "TestTeam2",
        assignedTo = assignedTo,
      ),
      buildFullPersonDto(
        crn = "CRN3",
        nomsNumber = "PRI_3",
        roshLevel = RiskLevel.MEDIUM,
        teamCode = "TestTeam1",
        assignedTo = assignedTo,
        limitedAccess = true,
      ),
      buildFullPersonDto(
        crn = "CRN4",
        nomsNumber = "PRI_4",
        roshLevel = RiskLevel.VERY_HIGH,
        teamCode = "TestTeam2",
        assignedTo = assignedTo,
        limitedAccess = true,
      ),
      buildLimitedPersonDto(
        crn = "CRN5",
        nomsNumber = "PRI_5",
        teamCode = "TestTeam1",
        assignedTo = assignedTo,
      ),
      buildLimitedPersonDto(crn = "CRN6", nomsNumber = "PRI_6", teamCode = "TestTeam3", assignedTo = assignedTo),
      buildFullPersonDto(
        crn = "CRN6",
        nomsNumber = "PRI_6",
        name = buildName(forename = "Other", middleName = "Users", surname = "Case"),
        roshLevel = RiskLevel.VERY_HIGH,
        teamCode = "TestTeam2",
        assignedTo = assignedToOther,
      ),
    )

    private fun toLimitedCaseDto(crn: String, prisonNumber: String?, assignedTo: AssignedToDto) = CaseDto(
      name = null,
      dateOfBirth = null,
      crn = crn,
      prisonNumber = prisonNumber,
      photoUrl = null,
      tierScore = null,
      riskLevel = null,
      pncReference = null,
      assignedTo = assignedTo,
      currentAccommodation = null,
      nextAccommodation = null,
      status = null,
      actions = emptyList(),
      userAccess = UserAccess.LIMITED,
      limitedAccess = true,
    )

    @Test
    fun `returns full list when no filters provided, CaseDto is redacted when UserAccess is Limited`() {
      val result = caseQueryService.getCases(personDtos = personDtos)
      assertThat(result).hasSize(6)

      val limitedCases = result.filter { it.userAccess == UserAccess.LIMITED }
      assertThat(limitedCases).hasSize(2)

      val limitedCaseDto1 = toLimitedCaseDto(crn = "CRN5", prisonNumber = "PRI_5", assignedTo = assignedTo)
      val limitedCaseDto2 = toLimitedCaseDto(crn = "CRN6", prisonNumber = "PRI_6", assignedTo = assignedTo)
      assertThat(limitedCases).containsExactly(limitedCaseDto1, limitedCaseDto2)
      assertThat(result).noneMatch { it.assignedTo!!.username == assignedToOther.username }
    }

    @ParameterizedTest
    @CsvSource(
      value = [
        "crn1,1,FULL",
        "CRN3,1,FULL",
        "cRn5,1,LIMITED",
        "crn,0,null", // attempted partial match
        "null,6,null",
        "'',6,null",
      ],
      nullValues = ["null"],
    )
    fun `filters by match on FULL CRN search, ignoring case`(
      searchTerm: String?,
      count: Int,
      userAccess: UserAccess?,
    ) {
      val result = caseQueryService.getCases(personDtos = personDtos, searchTerm = searchTerm)
      assertThat(result).hasSize(count)

      if (userAccess != null) {
        assertThat(result.size).isEqualTo(1)
        assertThat(result.first().crn).isEqualToIgnoringCase(searchTerm)
        assertThat(result.first().userAccess).isEqualTo(userAccess)
      }

      assertThat(result).noneMatch { it.assignedTo!!.username == assignedToOther.username }
    }

    @ParameterizedTest
    @CsvSource(
      value = [
        "pri_2,1,FULL",
        "PRI_4,1,FULL",
        "pRi_6,1,LIMITED",
        "pri_,0,null", // attempted partial match
        "null,6,null",
        "'',6,null",
      ],
      nullValues = ["null"],
    )
    fun `filters by match on FULL prisonNumber search, ignoring case`(
      searchTerm: String?,
      count: Int,
      userAccess: UserAccess?,
    ) {
      val result = caseQueryService.getCases(personDtos = personDtos, searchTerm = searchTerm)
      assertThat(result).hasSize(count)

      if (userAccess != null) {
        assertThat(result.size).isEqualTo(1)
        assertThat(result.first().prisonNumber).isEqualToIgnoringCase(searchTerm)
        assertThat(result.first().userAccess).isEqualTo(userAccess)
      }
      assertThat(result).noneMatch { it.assignedTo!!.username == assignedToOther.username }
    }

    @ParameterizedTest
    @CsvSource(
      value = [
        "xxxxx,0",
        "qqqqq,1",
        "fIrSt,3",
        " fIrSt ,3",
        "fI,3",
        "rSt,3",
        "rSt Mid,3",
        "First Middle Last,2", // Currently the search includes middle name. Will be changed in a future PR.
        "First Last,0",
        "MultiCaseSurname,1",
        "Multi,1",
        "CASE,1",
        "SurNaMe,1",
        "'',6",
        "null,6",
      ],
      nullValues = ["null"],
    )
    fun `filters by full and partial match on name ignoring case, does NOT return LIMITED LAO when a searchTerm IS provided but IS NOT a full CRN or PrisonNumber`(
      searchTerm: String?,
      count: Int,
    ) {
      val result = caseQueryService.getCases(personDtos = personDtos, searchTerm = searchTerm)
      assertThat(result).hasSize(count)
      if (!searchTerm.isNullOrEmpty()) {
        assertThat(result)
          .allMatch { it.name!!.contains(searchTerm, ignoreCase = true) }
          .noneMatch { it.userAccess == UserAccess.LIMITED }
      }
      assertThat(result).noneMatch { it.assignedTo!!.username == assignedToOther.username }
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
      if (riskLevel != null) {
        assertThat(result.map { it.userAccess }).noneMatch { it == UserAccess.LIMITED }
      }
      assertThat(result).noneMatch { it.assignedTo!!.username == assignedToOther.username }
    }

    @ParameterizedTest
    @CsvSource(
      "TestTeam1, 3", // FULL / FULL & limitedAccess / LIMITED
      "TestTeam2,3", // FULL / FULL & limitedAccess
      "TestTeam3,1", // LIMITED
      "TestTeam4,0",
    )
    fun `filters by team code`(teamCode: String, count: Int) {
      val result = caseQueryService.getCases(personDtos = personDtos, teamCode = teamCode)
      assertThat(result).hasSize(count)
      // check we can see cases in other teams
      if (teamCode == "TestTeam2") {
        assertThat(result.map { it.assignedTo!!.username }.distinct()).containsExactly(username, "Second.User")
      } else {
        assertThat(result).noneMatch { it.assignedTo!!.username == assignedToOther.username }
      }
    }

    @Test
    fun `does not call eligibility service when UserAccess is LIMITED`() {
      val limited = listOf(buildLimitedPersonDto(crn = "limited"))
      every { caseRepository.mapByCrns(any()) } returns emptyMap()
      caseQueryService.getCases(limited)
      verify(exactly = 0) {
        eligibilityService.getEligibility(any(), any(), any(), any())
      }
    }

    @Test
    fun `calls eligibility service for full person`() {
      val person = personDtos[0] as FullPersonDto
      val crn = person.crn

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
          crn = crn,
          gender = person.gender,
          caseEntity = any(),
          dutyToRefer = any(),
        )
      }
    }

    @Test
    fun `calls eligibility service for FULL person with limited access`() {
      val person = personDtos[3] as FullPersonDto
      val crn = person.crn

      every { caseRepository.mapByCrns(any()) } returns mapOf(crn to buildCaseEntity { withCrn(crn) })
      every { eligibilityService.getEligibility(any(), any(), any(), any()) } returns buildEligibilityDto(crn)
      every {
        dutyToReferQueryService.getDutyToRefer(
          any(CaseEntity::class),
          any(String::class),
        )
      } returns buildDutyToReferDto(crn)

      val result = caseQueryService.getCases(listOf(person))

      assertThat(result.single().limitedAccess).isTrue

      verify {
        eligibilityService.getEligibility(
          crn = crn,
          gender = person.gender,
          caseEntity = any(),
          dutyToRefer = any(),
        )
      }
    }
  }

  @Nested
  inner class GetCases {

    @BeforeEach
    fun setUp() {
      every { userService.getUsername() } returns Username(username)
    }

    @Test
    fun `should get cases as all cases from case table and populate missing data from personDtos`() {
      val crnList = listOf(crnOne, crnTwo)
      val staff = buildOfficer(username = username)
      val personDto1 = buildFullPersonDto(crn = crnOne, staff = staff)
      val personDto2 = buildFullPersonDto(crn = crnTwo, staff = staff)
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
