package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.customcaselist

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AssignedToDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseAccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.PeopleType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.RiskLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.UserAccess
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.exception.UpstreamFailureException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationSummariesDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.OrchestrationResultDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.UpstreamFailure
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.UpstreamFailureTransformer.toUpstreamFailureDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_USER_ACCESS
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.POST_CASE_SUMMARIES
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremisesanddelius.CaseSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationaccesscontrol.CaseAccess
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildManager
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildTeam
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildUserCustomCaseListEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildUserEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withCrn
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withPrisonNumber
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.UserCustomCaseListRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.UserService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.LimitedAccessService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.customcaselist.CustomCaseListOrchestrationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.customcaselist.CustomCaseListQueryService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildCustomCaseListOrchestrationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildUpstreamFailure
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@ExtendWith(MockKExtension::class)
class CustomCaseListQueryServiceTest {
  private companion object {
    const val DELIUS_USERNAME = "DELIUS_USER"
    const val DEFAULT_TEAM_CODE = "12345"
  }

  private val clock: Clock = Clock.fixed(Instant.parse("2026-10-01T00:00:00Z"), ZoneOffset.UTC)

  @MockK
  lateinit var userService: UserService

  @MockK
  lateinit var caseRepository: CaseRepository

  @MockK
  lateinit var userCustomCaseListRepository: UserCustomCaseListRepository

  @MockK
  lateinit var customCaseListOrchestrationService: CustomCaseListOrchestrationService

  @MockK
  lateinit var limitedAccessService: LimitedAccessService

  lateinit var customCaseListQueryService: CustomCaseListQueryService

  private val user = buildUserEntity(username = DELIUS_USERNAME)

  @BeforeEach
  fun setUp() {
    customCaseListQueryService = CustomCaseListQueryService(
      userService = userService,
      caseRepository = caseRepository,
      userCustomCaseListRepository = userCustomCaseListRepository,
      customCaseListOrchestrationService = customCaseListOrchestrationService,
      limitedAccessService = limitedAccessService,
      clock = clock,
    )
    every { userService.authorizeAndRetrieveUser() } returns user
    every { limitedAccessService.throwIfUserAccessFailed(any()) } just runs
  }

  @Test
  fun `returns an empty list without loading cases or checking access when the user has no custom case list`() {
    every { userCustomCaseListRepository.findAllBySasUserId(user.id) } returns emptyList()

    val result = customCaseListQueryService.getCustomCaseList()

    assertThat(result.data).isEmpty()
    verify(exactly = 0) { caseRepository.findAllWithIdentifiersByIdIn(any()) }
    verify(exactly = 0) { customCaseListOrchestrationService.getCaseAccessAndSummaries(any(), any()) }
  }

  @Test
  fun `maps the persisted case to a case dto`() {
    val accommodationSummaries = buildAccommodationSummariesDto(
      caseAccommodationStatus = CaseAccommodationStatus.TRANSIENT,
      caseAccommodationStatusDate = LocalDate.of(2026, 9, 1),
    )
    val caseEntity = buildCaseEntity(
      tierScore = "B2",
      firstName = "Joe",
      lastName = "Bloggs",
      dateOfBirth = LocalDate.of(1990, 1, 2),
      roshLevelCode = RiskLevel.HIGH.code,
      assignedToForename = "Probation",
      assignedToSurname = "Practitioner",
      assignedToUsername = DELIUS_USERNAME,
      accommodationSummariesDto = accommodationSummaries,
    ) {
      withCrn("A123456")
      withPrisonNumber("A1234AA")
    }
    val caseSummary = buildCaseSummary(crn = "A123456")
    stubCases(caseEntity)
    stubAccess(accessible = listOf("A123456"), caseSummaries = listOf(caseSummary))

    val caseDto = customCaseListQueryService.getCustomCaseList().data.single()

    assertThat(caseDto.crn).isEqualTo("A123456")
    assertThat(caseDto.prisonNumber).isEqualTo("A1234AA")
    assertThat(caseDto.forename).isEqualTo("Joe")
    assertThat(caseDto.surname).isEqualTo("Bloggs")
    assertThat(caseDto.dateOfBirth).isEqualTo(LocalDate.of(1990, 1, 2))
    assertThat(caseDto.tierScore).isEqualTo("B2")
    assertThat(caseDto.riskLevel).isEqualTo(RiskLevel.HIGH)
    assertThat(caseDto.userAccess).isEqualTo(UserAccess.FULL)
    assertThat(caseDto.limitedAccess).isFalse()
    assertThat(caseDto.assignedTo).isEqualTo(AssignedToDto(forename = "Probation", surname = "Practitioner", username = "DELIUS_USER"))
    assertThat(caseDto.pncReference).isNull()
    assertThat(caseDto.accommodationSummaries).isEqualTo(accommodationSummaries)
    assertThat(caseDto.teamCode).isEqualTo(caseSummary.manager.team.code)
  }

  @Test
  fun `returns null assigned to data when the case has not been refreshed`() {
    stubCases(buildCaseEntity { withCrn("A123456") })
    stubAccess(accessible = listOf("A123456"))

    assertThat(customCaseListQueryService.getCustomCaseList(teamCode = DEFAULT_TEAM_CODE).data.single().assignedTo).isNull()
  }

  @Test
  fun `sets limited access flag from the case summary exclusion and restriction fields`() {
    stubCases(
      buildCaseEntity(assignedToForename = "Probation", assignedToSurname = "Practitioner", assignedToUsername = DELIUS_USERNAME) { withCrn("A111111") },
      buildCaseEntity(assignedToForename = "Probation", assignedToSurname = "Practitioner", assignedToUsername = DELIUS_USERNAME) { withCrn("B222222") },
      buildCaseEntity(assignedToForename = "Probation", assignedToSurname = "Practitioner", assignedToUsername = DELIUS_USERNAME) { withCrn("C333333") },
    )
    stubAccess(
      accessible = listOf("A111111", "B222222", "C333333"),
      caseSummaries = listOf(
        buildCaseSummary(crn = "A111111"),
        buildCaseSummary(crn = "B222222", currentExclusion = true),
        buildCaseSummary(crn = "C333333", currentRestriction = true),
      ),
    )

    val limitedAccessByCrn = customCaseListQueryService.getCustomCaseList().data.associate { it.crn to it.limitedAccess }

    assertThat(limitedAccessByCrn).isEqualTo(mapOf("A111111" to false, "B222222" to true, "C333333" to true))
  }

  @Test
  fun `returns a limited case dto for a case missing from the case summaries`() {
    stubCases(buildCaseEntity(assignedToForename = "Probation", assignedToSurname = "Practitioner", assignedToUsername = DELIUS_USERNAME) { withCrn("A123456") })
    stubAccess(accessible = listOf("A123456"), caseSummaries = emptyList())

    assertThat(customCaseListQueryService.getCustomCaseList().data.single())
      .isEqualTo(CaseDto(crn = "A123456", userAccess = UserAccess.LIMITED, limitedAccess = true))
  }

  @Test
  fun `returns the cases as limited cases with an upstream failure when the case summaries call fails`() {
    stubCases(buildCaseEntity(assignedToForename = "Probation", assignedToSurname = "Practitioner", assignedToUsername = DELIUS_USERNAME) { withCrn("A123456") })
    stubAccess(accessible = listOf("A123456"), caseSummaries = emptyList(), upstreamFailures = listOf(buildUpstreamFailure(callKey = POST_CASE_SUMMARIES)))

    val result = customCaseListQueryService.getCustomCaseList()

    assertThat(result.data.single().crn).isEqualTo("A123456")
    assertThat(result.data.single().userAccess).isEqualTo(UserAccess.LIMITED)
    assertThat(result.data.single().limitedAccess).isTrue()
    assertThat(result.upstreamFailures.map { it.endpoint }).containsExactly(POST_CASE_SUMMARIES)
  }

  @Test
  fun `throws an upstream failure exception when the access check fails`() {
    val failure = buildUpstreamFailure(callKey = GET_USER_ACCESS + 0)
    stubCases(buildCaseEntity(assignedToForename = "Probation", assignedToSurname = "Practitioner", assignedToUsername = DELIUS_USERNAME) { withCrn("A123456") })
    every { customCaseListOrchestrationService.getCaseAccessAndSummaries(user.username, any()) } returns OrchestrationResultDto(
      data = buildCustomCaseListOrchestrationDto(),
      upstreamFailures = listOf(failure),
    )
    every { limitedAccessService.throwIfUserAccessFailed(listOf(failure)) } throws UpstreamFailureException(toUpstreamFailureDto(failure))

    assertThatThrownBy { customCaseListQueryService.getCustomCaseList() }
      .isInstanceOf(UpstreamFailureException::class.java)
  }

  @Test
  fun `checks access and requests case summaries for the logged in user against every crn in the custom case list`() {
    stubCases(buildCaseEntity(assignedToForename = "Probation", assignedToSurname = "Practitioner", assignedToUsername = DELIUS_USERNAME) { withCrn("A123456") }, buildCaseEntity(assignedToForename = "Probation", assignedToSurname = "Practitioner", assignedToUsername = DELIUS_USERNAME) { withCrn("B654321") })
    stubAccess(accessible = listOf("A123456", "B654321"))

    customCaseListQueryService.getCustomCaseList()

    verify {
      customCaseListOrchestrationService.getCaseAccessAndSummaries(DELIUS_USERNAME, match { it.toSet() == setOf("A123456", "B654321") })
    }
  }

  @Test
  fun `returns cases the user is excluded or restricted from as limited cases with only the crn and team code`() {
    stubCases(
      buildCaseEntity(assignedToForename = "Probation", assignedToSurname = "Practitioner", assignedToUsername = DELIUS_USERNAME, firstName = "Joe", lastName = "Bloggs") { withCrn("A123456") },
      buildCaseEntity(assignedToForename = "Probation", assignedToSurname = "Practitioner", assignedToUsername = DELIUS_USERNAME, firstName = "Jane", lastName = "Doe") { withCrn("B654321") },
      buildCaseEntity(assignedToForename = "Probation", assignedToSurname = "Practitioner", assignedToUsername = DELIUS_USERNAME, firstName = "John", lastName = "Smith") { withCrn("C111111") },
    )
    stubAccess(accessible = listOf("A123456"), excluded = listOf("B654321"), restricted = listOf("C111111"))

    val result = customCaseListQueryService.getCustomCaseList()

    val limitedCases = result.data.filter { it.crn in listOf("B654321", "C111111") }
    assertThat(limitedCases).containsExactlyInAnyOrder(
      CaseDto(crn = "B654321", userAccess = UserAccess.LIMITED, limitedAccess = true, teamCode = null),
      CaseDto(crn = "C111111", userAccess = UserAccess.LIMITED, limitedAccess = true, teamCode = null),
    )
    assertThat(result.data.single { it.crn == "A123456" }.userAccess).isEqualTo(UserAccess.FULL)
  }

  @Test
  fun `sorts limited cases to the top followed by accessible cases in the standard case list ordering`() {
    stubCases(
      buildCaseEntity(assignedToForename = "Probation", assignedToSurname = "Practitioner", assignedToUsername = DELIUS_USERNAME, lastName = "Settled", accommodationSummariesDto = buildAccommodationSummariesDto(CaseAccommodationStatus.SETTLED)) { withCrn("A111111") },
      buildCaseEntity(assignedToForename = "Probation", assignedToSurname = "Practitioner", assignedToUsername = DELIUS_USERNAME, lastName = "Risk", accommodationSummariesDto = buildAccommodationSummariesDto(CaseAccommodationStatus.RISK_OF_NO_FIXED_ABODE)) { withCrn("B222222") },
      buildCaseEntity(assignedToForename = "Probation", assignedToSurname = "Practitioner", assignedToUsername = DELIUS_USERNAME) { withCrn("C333333") },
      buildCaseEntity(assignedToForename = "Probation", assignedToSurname = "Practitioner", assignedToUsername = DELIUS_USERNAME) { withCrn("D444444") },
    )
    stubAccess(accessible = listOf("A111111", "B222222"), excluded = listOf("D444444"), restricted = listOf("C333333"))

    val result = customCaseListQueryService.getCustomCaseList()

    assertThat(result.data.map { it.crn }).containsExactly("C333333", "D444444", "B222222", "A111111")
  }

  @Test
  fun `includes partially populated cases with blank fields in the standard case list ordering`() {
    val partial = buildCaseEntity(assignedToForename = "Probation", assignedToSurname = "Practitioner", assignedToUsername = DELIUS_USERNAME, tierScore = null, firstName = "Aaron", lastName = "Aardvark") { withCrn("A111111") }
    val populated = buildCaseEntity(assignedToForename = "Probation", assignedToSurname = "Practitioner", assignedToUsername = DELIUS_USERNAME, firstName = "Zed", lastName = "Zebra") { withCrn("B222222") }
    stubCases(populated, partial)
    stubAccess(accessible = listOf("A111111", "B222222"))

    val result = customCaseListQueryService.getCustomCaseList()

    assertThat(result.data.map { it.crn }).containsExactly("A111111", "B222222")
    assertThat(result.data.first().tierScore).isNull()
  }

  @Test
  fun `sorts refreshed cases using the standard case list ordering`() {
    stubCases(
      buildCaseEntity(assignedToForename = "Probation", assignedToSurname = "Practitioner", assignedToUsername = DELIUS_USERNAME, lastName = "Settled", accommodationSummariesDto = buildAccommodationSummariesDto(CaseAccommodationStatus.SETTLED)) { withCrn("A111111") },
      buildCaseEntity(assignedToForename = "Probation", assignedToSurname = "Practitioner", assignedToUsername = DELIUS_USERNAME, lastName = "Risk", accommodationSummariesDto = buildAccommodationSummariesDto(CaseAccommodationStatus.RISK_OF_NO_FIXED_ABODE)) { withCrn("B222222") },
      buildCaseEntity(assignedToForename = "Probation", assignedToSurname = "Practitioner", assignedToUsername = DELIUS_USERNAME, lastName = "None", accommodationSummariesDto = null) { withCrn("C333333") },
      buildCaseEntity(assignedToForename = "Probation", assignedToSurname = "Practitioner", assignedToUsername = DELIUS_USERNAME, lastName = "Nfa", accommodationSummariesDto = buildAccommodationSummariesDto(CaseAccommodationStatus.NO_FIXED_ABODE)) { withCrn("D444444") },
    )
    stubAccess(accessible = listOf("A111111", "B222222", "C333333", "D444444"))

    val result = customCaseListQueryService.getCustomCaseList()

    assertThat(result.data.map { it.crn }).containsExactly("C333333", "B222222", "D444444", "A111111")
  }

  @Test
  fun `returns the crns in the users custom case list without checking access`() {
    every { userCustomCaseListRepository.findLatestCrnsBySasUserId(user.id) } returns listOf("A111111", "B222222")

    val result = customCaseListQueryService.getCustomCaseListCrns()

    assertThat(result.data).containsExactly("A111111", "B222222")
    verify(exactly = 0) { customCaseListOrchestrationService.getCaseAccessAndSummaries(any(), any()) }
  }

  @Nested
  inner class Filtering {
    private val highRisk = RiskLevel.HIGH.code
    private val lowRisk = RiskLevel.LOW.code

    private fun summary(crn: String, teamCode: String = DEFAULT_TEAM_CODE) = buildCaseSummary(crn = crn, manager = buildManager(buildTeam(code = teamCode)))

    private fun crns(
      searchTerm: String? = null,
      riskLevel: RiskLevel? = null,
      teamCode: String? = null,
      peopleType: PeopleType? = null,
    ) = customCaseListQueryService
      .getCustomCaseList(searchTerm = searchTerm, riskLevel = riskLevel, teamCode = teamCode, peopleType = peopleType)
      .data
      .map { it.crn }

    @Test
    fun `does not filter by assignee when no team code is provided`() {
      stubCases(
        buildCaseEntity(assignedToForename = "Probation", assignedToSurname = "Practitioner", assignedToUsername = DELIUS_USERNAME) { withCrn("A111111") },
        buildCaseEntity(assignedToForename = "Probation", assignedToSurname = "Practitioner", assignedToUsername = "OTHER.USER") { withCrn("B222222") },
        buildCaseEntity { withCrn("C333333") },
      )
      stubAccess(accessible = listOf("A111111", "B222222", "C333333"))

      assertThat(crns()).containsExactlyInAnyOrder("A111111", "B222222", "C333333")
    }

    @Test
    fun `filters by team code using the team on the case summary, regardless of assignee`() {
      stubCases(
        buildCaseEntity(assignedToForename = "Probation", assignedToSurname = "Practitioner", assignedToUsername = DELIUS_USERNAME) { withCrn("A111111") },
        buildCaseEntity(assignedToForename = "Probation", assignedToSurname = "Practitioner", assignedToUsername = "OTHER.USER") { withCrn("B222222") },
        buildCaseEntity(assignedToForename = "Probation", assignedToSurname = "Practitioner", assignedToUsername = DELIUS_USERNAME) { withCrn("C333333") },
      )
      stubAccess(
        accessible = listOf("A111111", "B222222", "C333333"),
        caseSummaries = listOf(
          summary("A111111", teamCode = "TEAM_A"),
          summary("B222222", teamCode = "TEAM_A"),
          summary("C333333", teamCode = "TEAM_B"),
        ),
      )

      assertThat(crns(teamCode = "team_a")).containsExactlyInAnyOrder("A111111", "B222222")
      assertThat(crns(teamCode = "TEAM_Z")).isEmpty()
    }

    @Test
    fun `does not filter by team when team code is blank`() {
      stubCases(
        buildCaseEntity(assignedToForename = "Probation", assignedToSurname = "Practitioner", assignedToUsername = DELIUS_USERNAME) { withCrn("A111111") },
        buildCaseEntity(assignedToForename = "Probation", assignedToSurname = "Practitioner", assignedToUsername = "OTHER.USER") { withCrn("B222222") },
      )
      stubAccess(accessible = listOf("A111111", "B222222"))

      assertThat(crns(teamCode = " ")).containsExactlyInAnyOrder("A111111", "B222222")
    }

    @Test
    fun `filters by risk level`() {
      stubCases(
        buildCaseEntity(assignedToForename = "Probation", assignedToSurname = "Practitioner", assignedToUsername = DELIUS_USERNAME, roshLevelCode = highRisk) { withCrn("A111111") },
        buildCaseEntity(assignedToForename = "Probation", assignedToSurname = "Practitioner", assignedToUsername = DELIUS_USERNAME, roshLevelCode = lowRisk) { withCrn("B222222") },
        buildCaseEntity(assignedToForename = "Probation", assignedToSurname = "Practitioner", assignedToUsername = DELIUS_USERNAME, roshLevelCode = null) { withCrn("C333333") },
      )
      stubAccess(accessible = listOf("A111111", "B222222", "C333333"))

      assertThat(crns(riskLevel = RiskLevel.HIGH)).containsExactly("A111111")
      assertThat(crns(riskLevel = RiskLevel.VERY_HIGH)).isEmpty()
    }

    @Test
    fun `filters by crn and by name search terms ignoring case`() {
      stubCases(
        buildCaseEntity(assignedToForename = "Probation", assignedToSurname = "Practitioner", assignedToUsername = DELIUS_USERNAME, firstName = "Joe", lastName = "Bloggs") { withCrn("A111111") },
        buildCaseEntity(assignedToForename = "Probation", assignedToSurname = "Practitioner", assignedToUsername = DELIUS_USERNAME, firstName = "Jane", lastName = "Doe") { withCrn("B222222") },
      )
      stubAccess(accessible = listOf("A111111", "B222222"))

      assertThat(crns(searchTerm = "a111111")).containsExactly("A111111")
      assertThat(crns(searchTerm = "bLoG")).containsExactly("A111111")
      assertThat(crns(searchTerm = "jane doe")).containsExactly("B222222")
      assertThat(crns(searchTerm = "nobody")).isEmpty()
      assertThat(crns(searchTerm = " ")).containsExactlyInAnyOrder("A111111", "B222222")
    }

    @Test
    fun `filters by people type using the accommodation status`() {
      stubCases(
        buildCaseEntity(assignedToForename = "Probation", assignedToSurname = "Practitioner", assignedToUsername = DELIUS_USERNAME, accommodationSummariesDto = buildAccommodationSummariesDto(CaseAccommodationStatus.SETTLED)) { withCrn("A111111") },
        buildCaseEntity(assignedToForename = "Probation", assignedToSurname = "Practitioner", assignedToUsername = DELIUS_USERNAME, accommodationSummariesDto = buildAccommodationSummariesDto(CaseAccommodationStatus.TRANSIENT)) { withCrn("B222222") },
        buildCaseEntity(assignedToForename = "Probation", assignedToSurname = "Practitioner", assignedToUsername = DELIUS_USERNAME, accommodationSummariesDto = null) { withCrn("C333333") },
      )
      stubAccess(accessible = listOf("A111111", "B222222", "C333333"))

      assertThat(crns(peopleType = PeopleType.HOUSED)).containsExactly("A111111")
      assertThat(crns(peopleType = PeopleType.NFA_RISK)).containsExactlyInAnyOrder("B222222", "C333333")
      assertThat(crns(peopleType = null)).hasSize(3)
    }

    @Test
    fun `combines all filters`() {
      stubCases(
        buildCaseEntity(assignedToForename = "Probation", assignedToSurname = "Practitioner", assignedToUsername = "OTHER.USER", firstName = "Alice", roshLevelCode = highRisk, accommodationSummariesDto = buildAccommodationSummariesDto(CaseAccommodationStatus.SETTLED)) { withCrn("A111111") },
        buildCaseEntity(assignedToForename = "Probation", assignedToSurname = "Practitioner", assignedToUsername = "OTHER.USER", firstName = "Alice", roshLevelCode = lowRisk, accommodationSummariesDto = buildAccommodationSummariesDto(CaseAccommodationStatus.SETTLED)) { withCrn("B222222") },
        buildCaseEntity(assignedToForename = "Probation", assignedToSurname = "Practitioner", assignedToUsername = "OTHER.USER", firstName = "Bob", roshLevelCode = highRisk, accommodationSummariesDto = buildAccommodationSummariesDto(CaseAccommodationStatus.SETTLED)) { withCrn("C333333") },
        buildCaseEntity(assignedToForename = "Probation", assignedToSurname = "Practitioner", assignedToUsername = "OTHER.USER", firstName = "Alice", roshLevelCode = highRisk, accommodationSummariesDto = buildAccommodationSummariesDto(CaseAccommodationStatus.TRANSIENT)) { withCrn("D444444") },
        buildCaseEntity(assignedToForename = "Probation", assignedToSurname = "Practitioner", assignedToUsername = "OTHER.USER", firstName = "Alice", roshLevelCode = highRisk, accommodationSummariesDto = buildAccommodationSummariesDto(CaseAccommodationStatus.SETTLED)) { withCrn("E555555") },
      )
      stubAccess(
        accessible = listOf("A111111", "B222222", "C333333", "D444444", "E555555"),
        caseSummaries = listOf(
          summary("A111111"),
          summary("B222222"),
          summary("C333333"),
          summary("D444444"),
          summary("E555555", teamCode = "OTHER_TEAM"),
        ),
      )

      assertThat(
        crns(searchTerm = "alice", riskLevel = RiskLevel.HIGH, teamCode = DEFAULT_TEAM_CODE, peopleType = PeopleType.HOUSED),
      ).containsExactly("A111111")
    }

    @Test
    fun `sorts the filtered cases using the standard case list ordering`() {
      stubCases(
        buildCaseEntity(assignedToForename = "Probation", assignedToSurname = "Practitioner", assignedToUsername = DELIUS_USERNAME, lastName = "Settled", accommodationSummariesDto = buildAccommodationSummariesDto(CaseAccommodationStatus.SETTLED)) { withCrn("A111111") },
        buildCaseEntity(assignedToForename = "Probation", assignedToSurname = "Practitioner", assignedToUsername = DELIUS_USERNAME, lastName = "Risk", accommodationSummariesDto = buildAccommodationSummariesDto(CaseAccommodationStatus.RISK_OF_NO_FIXED_ABODE)) { withCrn("B222222") },
        buildCaseEntity(assignedToForename = "Probation", assignedToSurname = "Practitioner", assignedToUsername = DELIUS_USERNAME, lastName = "Nfa", accommodationSummariesDto = buildAccommodationSummariesDto(CaseAccommodationStatus.NO_FIXED_ABODE)) { withCrn("C333333") },
      )
      stubAccess(accessible = listOf("A111111", "B222222", "C333333"))

      assertThat(crns()).containsExactly("B222222", "C333333", "A111111")
    }
  }

  private fun stubCases(vararg cases: CaseEntity) {
    every { userCustomCaseListRepository.findAllBySasUserId(user.id) } returns
      cases.map { buildUserCustomCaseListEntity(sasUserId = user.id, sasCaseId = it.id) }
    every { caseRepository.findAllWithIdentifiersByIdIn(cases.map { it.id }) } returns cases.toList()
  }

  private fun stubAccess(
    accessible: List<String> = emptyList(),
    excluded: List<String> = emptyList(),
    restricted: List<String> = emptyList(),
    caseSummaries: List<CaseSummary> = (accessible + excluded + restricted).map { buildCaseSummary(crn = it) },
    upstreamFailures: List<UpstreamFailure> = emptyList(),
  ) {
    val caseAccess = accessible.map { CaseAccess(crn = it, userExcluded = false, userRestricted = false) } +
      excluded.map { CaseAccess(crn = it, userExcluded = true, userRestricted = false) } +
      restricted.map { CaseAccess(crn = it, userExcluded = false, userRestricted = true) }
    every { customCaseListOrchestrationService.getCaseAccessAndSummaries(user.username, any()) } returns OrchestrationResultDto(
      data = buildCustomCaseListOrchestrationDto(caseAccess = caseAccess, caseSummaries = caseSummaries),
      upstreamFailures = upstreamFailures,
    )
    every { limitedAccessService.getLimitedAccessCrns(any(), caseAccess) } returns (excluded + restricted).toSet()
  }
}
