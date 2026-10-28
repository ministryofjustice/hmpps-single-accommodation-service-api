package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.case

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AssignedToDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseAccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.RiskLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.UserAccess
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.exception.UpstreamFailureException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.OrchestrationResultDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.UpstreamFailure
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CASE_LIST
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_USER_ACCESS
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.sasanddelius.Case
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildName
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildOfficer
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildRoshLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildUserEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withCrn
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.UserService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.CaseOrchestrationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.CaseQueryService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.CaseTransformer.toCaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.FullPersonDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.LimitedAccessService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.LimitedPersonDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.PersonTransformer.toPersonDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildCaseListOrchestrationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildCaseOrchestrationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildFullPersonDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildLimitedPersonDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildUpstreamFailure
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

@ExtendWith(MockKExtension::class)
class CaseQueryServiceTest {
  private val clock: Clock = Clock.fixed(Instant.parse("2026-10-01T00:00:00Z"), ZoneOffset.UTC)

  @MockK
  lateinit var caseOrchestrationService: CaseOrchestrationService

  @MockK
  lateinit var userService: UserService

  @MockK
  lateinit var caseRepository: CaseRepository

  @MockK
  lateinit var limitedAccessService: LimitedAccessService

  lateinit var caseQueryService: CaseQueryService

  @BeforeEach
  fun setUpCaseQueryService() {
    caseQueryService = CaseQueryService(
      caseOrchestrationService = caseOrchestrationService,
      limitedAccessService = limitedAccessService,
      userService = userService,
      caseRepository = caseRepository,
      clock = clock,
    )
  }

  private val crnOne = "X12345"
  private val crnTwo = "X12346"
  private val crnThree = "X12347"
  private val crnFour = "X12348"
  private val crnFive = "X12349"
  private val crnSix = "X12350"
  private val crnSeven = "X12351"
  private val crnEight = "X12352"
  private val crnNine = "X12353"
  private val username = "user1"

  val assignedTo = AssignedToDto(
    forename = "Firstname",
    surname = "Surname",
    username = username,
  )

  val assignedToOther = AssignedToDto(
    forename = "Second",
    surname = "User",
    username = "Second.User",
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

  @Nested
  inner class GetCaseList {

    @Test
    fun `should get case list`() {
      val case1 = buildCase(crn = crnOne, name = buildName("Dave"), nomsNumber = "13234")
      val roshLevel = buildRoshLevel(code = "RMRH", description = "Medium Risk")
      val case2 = buildCase(crn = crnTwo, name = buildName("Bob"), nomsNumber = "12234", roshLevel = roshLevel)

      val caseList = OrchestrationResultDto(data = buildCaseListOrchestrationDto(cases = listOf(case1, case2)))

      every { userService.authorizeAndRetrieveUser() } returns buildUserEntity(username = username)

      every { caseOrchestrationService.getCaseList(username, "testTeam") } returns caseList
      every { limitedAccessService.getLimitedAccessCrns(any(), any()) } returns emptySet()

      val result = caseQueryService.getCaseList("testTeam")
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
      assertThat(firstPerson.gender).isEqualTo(case1.gender)
      assertThat(firstPerson.riskLevel).isEqualTo(RiskLevel.VERY_HIGH)

      val lastPerson = result.data.last() as FullPersonDto
      assertThat(lastPerson.crn).isEqualTo(crnTwo)
      assertThat(lastPerson.name).isEqualTo(case2.name.fullName)
      assertThat(lastPerson.nomsNumber).isEqualTo(case2.nomsNumber)
      assertThat(lastPerson.pncNumber).isEqualTo(case2.pncNumber)
      assertThat(lastPerson.dateOfBirth).isEqualTo(case2.dateOfBirth)
      assertThat(lastPerson.assignedTo.forename).isEqualTo(case2.staff.name.forename)
      assertThat(lastPerson.assignedTo.surname).isEqualTo(case2.staff.name.surname)
      assertThat(lastPerson.assignedTo.username).isEqualTo(case2.staff.username)
      assertThat(lastPerson.gender).isEqualTo(case2.gender)
      assertThat(lastPerson.riskLevel).isEqualTo(RiskLevel.MEDIUM)
    }

    @Test
    fun `should return cases the limited access service marks as limited as a LimitedPersonDto`() {
      stubCaseList(
        cases = listOf(buildCase(crn = crnOne), buildCase(crn = crnTwo)),
        limitedCrns = setOf(crnTwo),
      )

      val result = caseQueryService.getCaseList(null)

      assertThat(result.data.single { it.crn == crnOne }).isInstanceOf(FullPersonDto::class.java)
      assertThat(result.data.single { it.crn == crnTwo }).isInstanceOf(LimitedPersonDto::class.java)
    }

    @Test
    fun `should throw an upstream failure exception when any access check batch fails`() {
      stubCaseList(
        cases = listOf(buildCase(crn = crnOne)),
        upstreamFailures = listOf(buildUpstreamFailure(callKey = GET_USER_ACCESS + 1)),
      )

      assertThatThrownBy { caseQueryService.getCaseList(null) }
        .isInstanceOf(UpstreamFailureException::class.java)
    }

    @Test
    fun `should return the case list with upstream failures when a Delius page fails`() {
      stubCaseList(
        cases = listOf(buildCase(crn = crnOne)),
        upstreamFailures = listOf(buildUpstreamFailure(callKey = GET_CASE_LIST + username + 1)),
      )

      val result = caseQueryService.getCaseList(null)

      assertThat(result.data).hasSize(1)
      assertThat(result.upstreamFailures).hasSize(1)
    }

    private fun stubCaseList(
      cases: List<Case>,
      limitedCrns: Set<String> = emptySet(),
      upstreamFailures: List<UpstreamFailure> = emptyList(),
    ) {
      val caseListOrchestrationDto = buildCaseListOrchestrationDto(cases = cases)
      every { userService.authorizeAndRetrieveUser() } returns buildUserEntity(username = username)
      every { caseOrchestrationService.getCaseList(username, null) } returns OrchestrationResultDto(
        data = caseListOrchestrationDto,
        upstreamFailures = upstreamFailures,
      )
      every { limitedAccessService.getLimitedAccessCrns(cases.map { it.crn }, caseListOrchestrationDto.caseAccess) } returns limitedCrns
    }
  }

  @Nested
  inner class GetCases {

    private fun toLimitedCaseDto(crn: String) = CaseDto(
      dateOfBirth = null,
      crn = crn,
      prisonNumber = null,
      photoUrl = null,
      tierScore = null,
      riskLevel = null,
      pncReference = null,
      assignedTo = null,
      userAccess = UserAccess.LIMITED,
      limitedAccess = true,
    )

    @Test
    fun `CaseDto is redacted when UserAccess is Limited`() {
      every { caseRepository.mapByCrns(any()) } returns emptyMap()

      val result = caseQueryService.getCaseDtos(personDtos = personDtos)
      assertThat(result).hasSize(personDtos.size)

      val limitedCases = result.filter { it.userAccess == UserAccess.LIMITED }
      assertThat(limitedCases).hasSize(2)

      val limitedCaseDto1 = toLimitedCaseDto(crn = "CRN5")
      val limitedCaseDto2 = toLimitedCaseDto(crn = "CRN6")
      assertThat(limitedCases).containsExactly(limitedCaseDto1, limitedCaseDto2)
    }

    @Test
    fun `should get cases as all cases from case table and populate missing data from personDtos`() {
      val limitedCrn = "limitedCrn"
      val crnList = listOf(crnOne, crnTwo, limitedCrn)
      val staff = buildOfficer(username = username)
      val personDto1 = buildFullPersonDto(crn = crnOne, staff = staff)
      val personDto2 = buildFullPersonDto(crn = crnTwo, staff = staff)
      val personDto3 = buildLimitedPersonDto(crn = limitedCrn, staff = staff)
      val personDtos = listOf(
        personDto1,
        personDto2,
        personDto3,
      )
      val caseEntity1 = buildCaseEntity {
        withCrn(crnOne)
        currentAccommodation = buildAccommodationSummaryDto(crn = crnOne)
        accommodationStatus = CaseAccommodationStatus.SETTLED
      }
      val caseEntity2 = buildCaseEntity { withCrn(crnTwo) }
      val caseEntity3 = buildCaseEntity { withCrn(limitedCrn) }
      val caseEntities = mapOf(crnOne to caseEntity1, crnTwo to caseEntity2, limitedCrn to caseEntity3)
      val caseDto1 = personDto1.toCaseDto(
        caseEntity = caseEntity1,
        currentAccommodation = buildAccommodationSummaryDto(crn = crnOne),
        nextAccommodation = null,
        teamCode = "TEAM1",
      )
      val caseDto2 = personDto2.toCaseDto(caseEntity = caseEntity2, currentAccommodation = null, nextAccommodation = null, teamCode = "TEAM1")
      val caseDto3 = personDto3.toCaseDto(caseEntity = caseEntity3, currentAccommodation = null, nextAccommodation = null, teamCode = "TEAM1")
      every { caseRepository.mapByCrns(crnList) } returns caseEntities

      val result = caseQueryService.getCaseDtos(personDtos = personDtos)

      assertThat(result).hasSize(3)
      assertThat(result.map { it to it.limitedAccess to it.userAccess })
        .contains(
          caseDto1 to false to UserAccess.FULL,
          caseDto2 to false to UserAccess.FULL,
          caseDto3 to true to UserAccess.LIMITED,
        )
    }

    @Test
    fun `should populate team code from the person dto for full access cases`() {
      val personDto = buildFullPersonDto(crn = crnOne, teamCode = "TeamX")
      every { caseRepository.mapByCrns(listOf(crnOne)) } returns emptyMap()

      val result = caseQueryService.getCaseDtos(personDtos = listOf(personDto))

      assertThat(result.single().teamCode).isEqualTo("TeamX")
    }

    @Test
    fun `should not populate team code for limited access cases`() {
      every { caseRepository.mapByCrns(listOf(crnOne)) } returns emptyMap()

      val result = caseQueryService.getCaseDtos(personDtos = listOf(buildLimitedPersonDto(crn = crnOne, teamCode = "TeamX")))

      assertThat(result.single().teamCode).isNull()
    }
  }

  @Nested
  inner class IsCaseRecordInDb {

    @Test
    fun `should return true when case record exists in db`() {
      every { caseRepository.findByCrn(crnOne) } returns buildCaseEntity { withCrn(crnOne) }

      val result = caseQueryService.getPersistedCase(crnOne)

      assertThat(result).isNotNull
    }

    @Test
    fun `should return false when case record does not exist in db`() {
      every { caseRepository.findByCrn(crnOne) } returns null

      val result = caseQueryService.getPersistedCase(crnOne)

      assertThat(result).isNull()
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

      val person = toPersonDto(caseOrchestrationDto.case!!, limited = false)

      val result = caseQueryService.getCase(crnOne)
      assertThat(result.data).isEqualTo(
        toCaseDto(
          crn = crnOne,
          person = person,
          cpr = caseOrchestrationDto.cpr,
          tier = caseOrchestrationDto.tier,
          sexualOffenceRegistrations = caseOrchestrationDto.sexualOffenceRegistrations,
        ),
      )
      assertThat(result.upstreamFailures).isEmpty()
    }

    @Test
    fun `should return case with upstream failures on partial success`() {
      every { userService.authorizeAndRetrieveUser() } returns buildUserEntity(username = username)
      val failures = listOf(
        buildUpstreamFailure(callKey = "getTierByCrn"),
      )
      val caseOrchestrationDto = buildCaseOrchestrationDto(crn = crnOne, tier = null)

      every { caseOrchestrationService.getCase(username, crnOne) } returns OrchestrationResultDto(
        data = caseOrchestrationDto,
        upstreamFailures = failures,
      )

      val result = caseQueryService.getCase(crnOne)
      assertThat(result.data.tierScore).isNull()
      assertThat(result.upstreamFailures).hasSize(1)
    }
  }

  @Nested
  inner class GetCaseFromDelius {

    @Test
    fun `should return case from delius with no upstream failures when all calls succeed`() {
      every { userService.authorizeAndRetrieveUser() } returns buildUserEntity(username = username)
      val caseOrchestrationDto = buildCaseOrchestrationDto(
        crn = crnOne,
        cpr = null,
        tier = null,
        case = buildCase(crnOne),
      )

      every { caseOrchestrationService.getCaseFromDelius(username, crnOne) } returns OrchestrationResultDto(
        data = caseOrchestrationDto,
      )

      val result = caseQueryService.getCaseFromDelius(crnOne)

      assertThat(result.data).isEqualTo(toPersonDto(caseOrchestrationDto.case!!, limited = false))
      assertThat(result.upstreamFailures).isEmpty()
    }

    @Test
    fun `should return null case from delius with upstream failures`() {
      every { userService.authorizeAndRetrieveUser() } returns buildUserEntity(username = username)
      val failures = listOf(
        buildUpstreamFailure(callKey = "getCase"),
      )
      val caseOrchestrationDto = buildCaseOrchestrationDto(
        crn = crnOne,
        cpr = null,
        tier = null,
        case = null,
      )

      every { caseOrchestrationService.getCaseFromDelius(username, crnOne) } returns OrchestrationResultDto(
        data = caseOrchestrationDto,
        upstreamFailures = failures,
      )

      val result = caseQueryService.getCaseFromDelius(crnOne)

      assertThat(result.data).isNull()
      assertThat(result.upstreamFailures).hasSize(1)
    }
  }
}
