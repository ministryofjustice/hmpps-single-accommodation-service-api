package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.case

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationSummariesDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AssignedToDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseAccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.PeopleType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.RiskLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.UserAccess
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.exception.UpstreamFailureException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.OrchestrationResultDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.UpstreamFailure
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.UpstreamFailureTransformer.toUpstreamFailureDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CASE_LIST
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_USER_ACCESS
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationaccesscontrol.CaseAccess
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.sasanddelius.Case
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseTeam
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildName
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildOfficer
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildRoshLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildUserEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withCrn
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withPrisonNumber
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.UserService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.CaseOrchestrationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.CaseOrchestrationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.CaseQueryService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.CaseTransformer.toCaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.CaseWithUserAccess
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.LimitedAccessService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildCaseListOrchestrationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildCaseOrchestrationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildUpstreamFailure
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
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
    every { limitedAccessService.throwIfUserAccessFailed(any()) } just runs
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
      every { limitedAccessService.getCrnToUserAccess(any()) } answers { callOriginal() }

      val result = caseQueryService.getCaseList("testTeam")

      assertThat(result.data).containsExactly(
        CaseWithUserAccess(case1, UserAccess.FULL),
        CaseWithUserAccess(case2, UserAccess.FULL),
      )
    }

    @Test
    fun `should return limited user access for cases the limited access service marks as limited`() {
      stubCaseList(
        cases = listOf(buildCase(crn = crnOne), buildCase(crn = crnTwo)),
        limitedCrns = setOf(crnTwo),
      )

      val result = caseQueryService.getCaseList(null)

      assertThat(result.data.single { it.case.crn == crnOne }.userAccess).isEqualTo(UserAccess.FULL)
      assertThat(result.data.single { it.case.crn == crnTwo }.userAccess).isEqualTo(UserAccess.LIMITED)
    }

    @Test
    fun `should throw an upstream failure exception when any access check batch fails`() {
      val failure = buildUpstreamFailure(callKey = GET_USER_ACCESS + 1)
      stubCaseList(cases = listOf(buildCase(crn = crnOne)), upstreamFailures = listOf(failure))
      every { limitedAccessService.throwIfUserAccessFailed(listOf(failure)) } throws UpstreamFailureException(toUpstreamFailureDto(failure))

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

    @Test
    fun `should return no user access for a case with no entry in the access response`() {
      val case = buildCase(crn = crnOne)
      val dto = buildCaseListOrchestrationDto(cases = listOf(case), caseAccess = emptyList())
      every { userService.authorizeAndRetrieveUser() } returns buildUserEntity(username = username)
      every { caseOrchestrationService.getCaseList(username, null) } returns OrchestrationResultDto(data = dto)
      every { limitedAccessService.getCrnToUserAccess(any()) } answers { callOriginal() }

      val result = caseQueryService.getCaseList(null)

      assertThat(result.data.single().userAccess).isNull()
    }

    @Test
    fun `should return an empty list when there are no cases`() {
      stubCaseList(cases = emptyList())

      assertThat(caseQueryService.getCaseList(null).data).isEmpty()
    }

    private fun stubCaseList(
      cases: List<Case>,
      limitedCrns: Set<String> = emptySet(),
      upstreamFailures: List<UpstreamFailure> = emptyList(),
    ) {
      val dto = buildCaseListOrchestrationDto(cases = cases)
      val caseListOrchestrationDto = dto.copy(caseAccess = dto.caseAccess.excludingCrns(limitedCrns))
      every { userService.authorizeAndRetrieveUser() } returns buildUserEntity(username = username)
      every { caseOrchestrationService.getCaseList(username, null) } returns OrchestrationResultDto(
        data = caseListOrchestrationDto,
        upstreamFailures = upstreamFailures,
      )
      every { limitedAccessService.getCrnToUserAccess(any()) } answers { callOriginal() }
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

    private fun getCaseDtos(
      cases: List<CaseWithUserAccess>,
      searchTerm: String? = null,
      riskLevel: RiskLevel? = null,
      teamCode: String? = null,
      peopleType: PeopleType? = null,
      username: String? = null,
    ) = caseQueryService.getCaseDtos(
      cases = cases,
      searchTerm = searchTerm,
      riskLevel = riskLevel,
      teamCode = teamCode,
      peopleType = peopleType,
      username = username,
    )

    private fun fullAccess(case: Case) = CaseWithUserAccess(case, UserAccess.FULL)

    @Test
    fun `CaseDto is redacted when UserAccess is not FULL`() {
      val entity = buildCaseEntity(assignedToForename = "DbForename", assignedToSurname = "DbSurname", assignedToUsername = "DbUsername") { withCrn(crnTwo) }
      every { caseRepository.mapByCrns(any()) } returns mapOf(crnTwo to entity)

      val result = getCaseDtos(
        cases = listOf(
          fullAccess(buildCase(crn = crnOne)),
          CaseWithUserAccess(buildCase(crn = crnTwo), UserAccess.LIMITED),
          CaseWithUserAccess(buildCase(crn = crnThree), UserAccess.UNKNOWN),
          CaseWithUserAccess(buildCase(crn = crnFour), null),
        ),
      )

      assertThat(result).hasSize(4)
      assertThat(result.filter { it.userAccess == UserAccess.LIMITED }).containsExactlyInAnyOrder(
        toLimitedCaseDto(crn = crnTwo),
        toLimitedCaseDto(crn = crnThree),
        toLimitedCaseDto(crn = crnFour),
      )
    }

    @Test
    fun `should source case data from the case table and crn, team code and limited access from delius`() {
      val case = buildCase(
        crn = crnOne,
        nomsNumber = "DeliusPrisonNumber",
        roshLevel = buildRoshLevel(code = "RLRH", description = "Low Risk"),
        team = buildCaseTeam("TeamX"),
        limitedAccess = true,
      )
      val currentAccommodation = buildAccommodationSummaryDto(crn = crnOne)
      val entity = buildCaseEntity(
        firstName = "DbForename",
        lastName = "DbSurname",
        roshLevelCode = "RMRH",
        assignedToForename = "DbAssignedForename",
        assignedToSurname = "DbAssignedSurname",
        assignedToUsername = "DbAssignedUsername",
      ) {
        withCrn(crnOne)
        withPrisonNumber("DbPrisonNumber")
        this.currentAccommodation = currentAccommodation
        accommodationStatus = CaseAccommodationStatus.SETTLED
      }
      every { caseRepository.mapByCrns(listOf(crnOne)) } returns mapOf(crnOne to entity)

      val result = getCaseDtos(cases = listOf(fullAccess(case)))

      assertThat(result.single()).isEqualTo(
        CaseDto(
          forename = "DbForename",
          surname = "DbSurname",
          dateOfBirth = entity.dateOfBirth,
          crn = crnOne,
          prisonNumber = "DbPrisonNumber",
          tierScore = entity.tierScore,
          riskLevel = RiskLevel.MEDIUM,
          pncReference = null,
          assignedTo = AssignedToDto(forename = "DbAssignedForename", surname = "DbAssignedSurname", username = "DbAssignedUsername"),
          userAccess = UserAccess.FULL,
          limitedAccess = true,
          accommodationSummaries = AccommodationSummariesDto(
            caseAccommodationStatus = CaseAccommodationStatus.SETTLED,
            currentAccommodation = currentAccommodation,
          ),
          teamCode = "TeamX",
        ),
      )
    }

    @Test
    fun `should default limitedAccess to false when delius does not send it`() {
      every { caseRepository.mapByCrns(listOf(crnOne)) } returns emptyMap()

      val result = getCaseDtos(cases = listOf(fullAccess(buildCase(crn = crnOne).copy(limitedAccess = null))))

      assertThat(result.single().limitedAccess).isFalse()
    }

    @Test
    fun `should return a null risk level when the persisted rosh level code is unknown`() {
      every { caseRepository.mapByCrns(listOf(crnOne)) } returns mapOf(crnOne to buildCaseEntity(roshLevelCode = "UNKNOWN") { withCrn(crnOne) })

      val result = getCaseDtos(cases = listOf(fullAccess(buildCase(crn = crnOne))))

      assertThat(result.single().riskLevel).isNull()
    }

    @Test
    fun `should return a case with only delius data when there is no persisted row for the crn`() {
      every { caseRepository.mapByCrns(listOf(crnOne)) } returns emptyMap()

      val result = getCaseDtos(cases = listOf(fullAccess(buildCase(crn = crnOne, team = buildCaseTeam("TeamX")))))

      assertThat(result.single()).isEqualTo(
        CaseDto(
          crn = crnOne,
          assignedTo = AssignedToDto(forename = "First", surname = "Last", username = "user1"),
          userAccess = UserAccess.FULL,
          limitedAccess = false,
          teamCode = "TeamX",
        ),
      )
    }

    @Test
    fun `should only return cases assigned to the given username, falling back to delius when the row has no assigned user`() {
      val otherStaff = buildOfficer(username = "OtherUsername")
      every { caseRepository.mapByCrns(any()) } returns mapOf(
        crnOne to buildCaseEntity(assignedToForename = "DbForename", assignedToSurname = "DbSurname", assignedToUsername = username) { withCrn(crnOne) },
        crnTwo to buildCaseEntity(assignedToForename = "DbForename", assignedToSurname = "DbSurname", assignedToUsername = "OtherUsername") { withCrn(crnTwo) },
        crnThree to buildCaseEntity { withCrn(crnThree) },
        crnFour to buildCaseEntity { withCrn(crnFour) },
      )
      val cases = listOf(
        fullAccess(buildCase(crn = crnOne, staff = buildOfficer(username = username))),
        fullAccess(buildCase(crn = crnTwo, staff = buildOfficer(username = username))),
        fullAccess(buildCase(crn = crnThree, staff = buildOfficer(username = username))),
        fullAccess(buildCase(crn = crnFour, staff = otherStaff)),
      )

      assertThat(getCaseDtos(cases = cases, username = username).map { it.crn }).containsExactlyInAnyOrder(crnOne, crnThree)
      assertThat(getCaseDtos(cases = cases, username = null).map { it.crn }).containsExactlyInAnyOrder(crnOne, crnTwo, crnThree, crnFour)
    }

    @Test
    fun `should not populate team code for limited access cases`() {
      every { caseRepository.mapByCrns(listOf(crnOne)) } returns emptyMap()

      val result = getCaseDtos(cases = listOf(CaseWithUserAccess(buildCase(crn = crnOne, team = buildCaseTeam("TeamX")), UserAccess.LIMITED)))

      assertThat(result.single().teamCode).isNull()
    }

    @Test
    fun `should sort cases using today from the clock`() {
      val cases = listOf(crnOne, crnTwo).map { fullAccess(buildCase(crn = it)) }
      every { caseRepository.mapByCrns(listOf(crnOne, crnTwo)) } returns mapOf(
        crnOne to buildCaseEntity {
          withCrn(crnOne)
          accommodationStatus = CaseAccommodationStatus.TRANSIENT
          accommodationStatusDate = LocalDate.of(2026, 9, 1)
        },
        crnTwo to buildCaseEntity {
          withCrn(crnTwo)
          accommodationStatus = CaseAccommodationStatus.TRANSIENT
          accommodationStatusDate = LocalDate.of(2026, 10, 5)
        },
      )

      val result = getCaseDtos(cases = cases)

      // clock is fixed at 2026-10-01 so the future status date sorts before the past one
      assertThat(result.map { it.crn }).containsExactly(crnTwo, crnOne)
    }

    @Test
    fun `should apply the search, risk level, team code and people type filters`() {
      val teamX = buildCaseTeam("TeamX")
      val cases = listOf(
        fullAccess(buildCase(crn = crnOne, team = teamX)),
        fullAccess(buildCase(crn = crnTwo, team = teamX)),
        fullAccess(buildCase(crn = crnThree, team = buildCaseTeam("TeamY"))),
        fullAccess(buildCase(crn = crnFour, team = teamX)),
      )
      every { caseRepository.mapByCrns(any()) } returns listOf(crnOne, crnTwo, crnThree, crnFour).associateWith { crn ->
        buildCaseEntity(roshLevelCode = if (crn == crnTwo) RiskLevel.LOW.code else RiskLevel.HIGH.code) {
          withCrn(crn)
          accommodationStatus = if (crn == crnFour) CaseAccommodationStatus.TRANSIENT else CaseAccommodationStatus.SETTLED
        }
      }

      val result = getCaseDtos(
        cases = cases,
        riskLevel = RiskLevel.HIGH,
        teamCode = "TeamX",
        peopleType = PeopleType.HOUSED,
      )

      assertThat(result.map { it.crn }).containsExactly(crnOne)
    }

    @Test
    fun `should return an empty list when there are no cases`() {
      every { caseRepository.mapByCrns(emptyList()) } returns emptyMap()

      assertThat(getCaseDtos(cases = emptyList())).isEmpty()
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
      val caseOrchestrationDto = stubGetCase(buildCaseOrchestrationDto(crn = crnOne))

      val result = caseQueryService.getCase(crnOne)
      assertThat(result.data).isEqualTo(
        toCaseDto(
          crn = crnOne,
          case = caseOrchestrationDto.case,
          cpr = caseOrchestrationDto.cpr,
          tier = caseOrchestrationDto.tier,
          sexualOffenceRegistrations = caseOrchestrationDto.sexualOffenceRegistrations,
          userAccess = UserAccess.FULL,
        ),
      )
      assertThat(result.upstreamFailures).isEmpty()
    }

    @Test
    fun `should return case with upstream failures on partial success`() {
      stubGetCase(
        buildCaseOrchestrationDto(crn = crnOne, tier = null),
        upstreamFailures = listOf(buildUpstreamFailure(callKey = "getTierByCrn")),
      )

      val result = caseQueryService.getCase(crnOne)
      assertThat(result.data.tierScore).isNull()
      assertThat(result.upstreamFailures).hasSize(1)
    }

    @Test
    fun `should return a limited case when the limited access service marks the crn as limited`() {
      stubGetCase(buildCaseOrchestrationDto(crn = crnOne), limitedCrns = setOf(crnOne))

      val result = caseQueryService.getCase(crnOne)

      assertThat(result.data).isEqualTo(CaseDto(crn = crnOne, userAccess = UserAccess.LIMITED, limitedAccess = true))
    }

    @Test
    fun `should return a full case with limitedAccess when the user can see a case that has exclusions or restrictions`() {
      stubGetCase(buildCaseOrchestrationDto(crn = crnOne, case = buildCase(crn = crnOne, limitedAccess = true)))

      val result = caseQueryService.getCase(crnOne)

      assertThat(result.data.userAccess).isEqualTo(UserAccess.FULL)
      assertThat(result.data.limitedAccess).isTrue()
    }

    @Test
    fun `should throw an upstream failure exception when the access check fails`() {
      val failure = buildUpstreamFailure(callKey = GET_USER_ACCESS + 0)
      stubGetCase(buildCaseOrchestrationDto(crn = crnOne, caseAccess = emptyList()), upstreamFailures = listOf(failure))
      every { limitedAccessService.throwIfUserAccessFailed(listOf(failure)) } throws UpstreamFailureException(toUpstreamFailureDto(failure))

      assertThatThrownBy { caseQueryService.getCase(crnOne) }
        .isInstanceOf(UpstreamFailureException::class.java)
    }

    @Test
    fun `should return a redacted limited case when the crn has no entry in the access response`() {
      stubGetCase(buildCaseOrchestrationDto(crn = crnOne, caseAccess = emptyList()))

      val result = caseQueryService.getCase(crnOne)

      assertThat(result.data).isEqualTo(CaseDto(crn = crnOne, userAccess = UserAccess.LIMITED, limitedAccess = true))
    }

    @Test
    fun `should return a case with unknown access when delius returns no case`() {
      stubGetCase(buildCaseOrchestrationDto(crn = crnOne, case = null))

      val result = caseQueryService.getCase(crnOne)

      assertThat(result.data).isEqualTo(CaseDto(crn = crnOne, userAccess = UserAccess.UNKNOWN, limitedAccess = null))
    }

    private fun stubGetCase(
      caseOrchestrationDto: CaseOrchestrationDto,
      limitedCrns: Set<String> = emptySet(),
      upstreamFailures: List<UpstreamFailure> = emptyList(),
    ): CaseOrchestrationDto {
      val stubbedDto = caseOrchestrationDto.copy(caseAccess = caseOrchestrationDto.caseAccess.excludingCrns(limitedCrns))
      every { userService.authorizeAndRetrieveUser() } returns buildUserEntity(username = username)
      every { caseOrchestrationService.getCase(username, crnOne) } returns OrchestrationResultDto(
        data = stubbedDto,
        upstreamFailures = upstreamFailures,
      )
      every { limitedAccessService.getCrnToUserAccess(any()) } answers { callOriginal() }
      return stubbedDto
    }
  }

  @Nested
  inner class GetCaseFromDelius {

    @Test
    fun `should return case from delius with no upstream failures when all calls succeed`() {
      val caseOrchestrationDto = stubGetCaseFromDelius(buildCaseOrchestrationDto(crn = crnOne, cpr = null, tier = null))

      val result = caseQueryService.getCaseFromDelius(crnOne)

      assertThat(result.data).isEqualTo(CaseWithUserAccess(caseOrchestrationDto.case!!, UserAccess.FULL))
      assertThat(result.upstreamFailures).isEmpty()
    }

    @Test
    fun `should return null case from delius with upstream failures`() {
      stubGetCaseFromDelius(
        buildCaseOrchestrationDto(crn = crnOne, cpr = null, tier = null, case = null),
        upstreamFailures = listOf(buildUpstreamFailure(callKey = "getCase")),
      )

      val result = caseQueryService.getCaseFromDelius(crnOne)

      assertThat(result.data).isNull()
      assertThat(result.upstreamFailures).hasSize(1)
    }

    @Test
    fun `should return limited user access when the limited access service marks the crn as limited`() {
      stubGetCaseFromDelius(buildCaseOrchestrationDto(crn = crnOne, cpr = null, tier = null), limitedCrns = setOf(crnOne))

      val result = caseQueryService.getCaseFromDelius(crnOne)

      assertThat(result.data!!.userAccess).isEqualTo(UserAccess.LIMITED)
    }

    @Test
    fun `should throw an upstream failure exception when the access check fails`() {
      val failure = buildUpstreamFailure(callKey = GET_USER_ACCESS + 0)
      stubGetCaseFromDelius(
        buildCaseOrchestrationDto(crn = crnOne, cpr = null, tier = null, caseAccess = emptyList()),
        upstreamFailures = listOf(failure),
      )
      every { limitedAccessService.throwIfUserAccessFailed(listOf(failure)) } throws UpstreamFailureException(toUpstreamFailureDto(failure))

      assertThatThrownBy { caseQueryService.getCaseFromDelius(crnOne) }
        .isInstanceOf(UpstreamFailureException::class.java)
    }

    @Test
    fun `should return no user access when the crn has no entry in the access response`() {
      stubGetCaseFromDelius(buildCaseOrchestrationDto(crn = crnOne, cpr = null, tier = null, caseAccess = emptyList()))

      assertThat(caseQueryService.getCaseFromDelius(crnOne).data!!.userAccess).isNull()
    }

    private fun stubGetCaseFromDelius(
      caseOrchestrationDto: CaseOrchestrationDto,
      limitedCrns: Set<String> = emptySet(),
      upstreamFailures: List<UpstreamFailure> = emptyList(),
    ): CaseOrchestrationDto {
      val stubbedDto = caseOrchestrationDto.copy(caseAccess = caseOrchestrationDto.caseAccess.excludingCrns(limitedCrns))
      every { userService.authorizeAndRetrieveUser() } returns buildUserEntity(username = username)
      every { caseOrchestrationService.getCaseFromDelius(username, crnOne) } returns OrchestrationResultDto(
        data = stubbedDto,
        upstreamFailures = upstreamFailures,
      )
      every { limitedAccessService.getCrnToUserAccess(any()) } answers { callOriginal() }
      return stubbedDto
    }
  }

  private fun List<CaseAccess>.excludingCrns(crns: Set<String>) = map { if (it.crn in crns) it.copy(userExcluded = true) else it }
}
