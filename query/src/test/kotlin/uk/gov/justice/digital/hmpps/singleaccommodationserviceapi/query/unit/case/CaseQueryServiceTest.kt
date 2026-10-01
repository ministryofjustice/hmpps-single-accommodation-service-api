package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.case

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AssignedToDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseAccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.PeopleType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.RiskLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.UserAccess
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.OrchestrationResultDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildName
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildOfficer
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildRoshLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildUserEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withCrn
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.UserService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.Username
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.CaseOrchestrationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.CaseQueryService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.CaseTransformer.toCaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.FullPersonDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.PersonDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.PersonTransformer.toPersonDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildCaseOrchestrationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildFullPersonDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildLimitedPersonDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildUpstreamFailure
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@ExtendWith(MockKExtension::class)
class CaseQueryServiceTest {
  private val clock: Clock = Clock.fixed(Instant.parse("2026-10-01T00:00:00Z"), ZoneOffset.UTC)
  private val today: LocalDate = LocalDate.now(clock)

  private data class SortCaseDefinition(
    val crn: String,
    val status: CaseAccommodationStatus,
    val statusDate: LocalDate? = null,
    val firstName: String = "First",
    val lastName: String = "Last",
    val hasCurrentAccommodation: Boolean = false,
  )

  @MockK
  lateinit var caseOrchestrationService: CaseOrchestrationService

  @MockK
  lateinit var userService: UserService

  @MockK
  lateinit var caseRepository: CaseRepository

  lateinit var caseQueryService: CaseQueryService

  @BeforeEach
  fun setUpCaseQueryService() {
    caseQueryService = CaseQueryService(
      caseOrchestrationService = caseOrchestrationService,
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

      val caseList = OrchestrationResultDto(data = listOf(case1, case2))

      every { userService.authorizeAndRetrieveUser() } returns buildUserEntity(username = username)

      every { caseOrchestrationService.getCaseList(username, "testTeam") } returns caseList

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
  }

  @Nested
  inner class CaseListFilters {

    @BeforeEach
    fun setup() {
      every { userService.getUsername() } returns Username(username)
    }

    @Test
    fun `returns only cases for the current user when no filters provided`() {
      val result = caseQueryService.applyCaseListFilters(
        personDtos = personDtos,
        searchTerm = null,
        riskLevel = null,
        teamCode = null,
      )
      assertThat(result).hasSize(6)
      assertThat(result).noneMatch { it.assignedTo.username == assignedToOther.username }
    }

    @Test
    fun `orders cases alphabetically by surname then forename by default`() {
      val unorderedPersonDtos = listOf(
        buildFullPersonDto(
          crn = "CRN1",
          name = buildName(forename = "CHARLIE", surname = "BROWN"),
          assignedTo = assignedTo,
        ),
        buildFullPersonDto(
          crn = "CRN2",
          name = buildName(forename = "Zoe", surname = "Adams"),
          assignedTo = assignedTo,
        ),
        buildLimitedPersonDto(
          crn = "CRN3",
          assignedTo = assignedTo,
        ),
        buildFullPersonDto(
          crn = "CRN4",
          name = buildName(forename = "alice", surname = "brown"),
          assignedTo = assignedTo,
        ),
      )

      val result = caseQueryService.applyCaseListFilters(personDtos = unorderedPersonDtos)

      assertThat(result.map { it.crn }).containsExactly("CRN2", "CRN4", "CRN1", "CRN3")
    }

    @ParameterizedTest
    @CsvSource(
      value = [
        "crn1,1",
        "CRN3,1",
        "cRn5,1",
        "crn,0", // attempted partial match
        "null,6",
        "'',6",
      ],
      nullValues = ["null"],
    )
    fun `filters by match on FULL CRN search, ignoring case`(
      searchTerm: String?,
      count: Int,
    ) {
      val result = caseQueryService.applyCaseListFilters(personDtos = personDtos, searchTerm = searchTerm)
      assertThat(result).hasSize(count)

      assertThat(result).noneMatch { it.assignedTo.username == assignedToOther.username }
    }

    @ParameterizedTest
    @CsvSource(
      value = [
        "pri_2,1",
        "PRI_4,1",
        "pRi_6,1",
        "pri_,0", // attempted partial match
        "null,6",
        "'',6",
      ],
      nullValues = ["null"],
    )
    fun `filters by match on FULL prisonNumber search, ignoring case`(
      searchTerm: String?,
      count: Int,
    ) {
      val result = caseQueryService.applyCaseListFilters(personDtos = personDtos, searchTerm = searchTerm)
      assertThat(result).hasSize(count)

      assertThat(result).noneMatch { it.assignedTo.username == assignedToOther.username }
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
        "First Middle Last,2",
        "First Last,2",
        "lAsT fIrSt,2",
        " Last   First ,2",
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
      val result = caseQueryService.applyCaseListFilters(personDtos = personDtos, searchTerm = searchTerm)
      assertThat(result).hasSize(count)
      assertThat(result).noneMatch { it.assignedTo.username == assignedToOther.username }
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
      val result = caseQueryService.applyCaseListFilters(personDtos = personDtos, riskLevel = riskLevel)
      assertThat(result).hasSize(count)
      assertThat(result).noneMatch { it.assignedTo.username == assignedToOther.username }
    }

    @ParameterizedTest
    @CsvSource(
      "TestTeam1, 3", // FULL / FULL & limitedAccess / LIMITED
      "TestTeam2,3", // FULL / FULL & limitedAccess
      "TestTeam3,1", // LIMITED
      "TestTeam4,0",
    )
    fun `filters by team code`(teamCode: String, count: Int) {
      val result = caseQueryService.applyCaseListFilters(personDtos = personDtos, teamCode = teamCode)
      assertThat(result).hasSize(count)
      // check we can see cases in other teams
      if (teamCode == "TestTeam2") {
        assertThat(result.mapNotNull { it.assignedTo.username }.distinct()).containsExactlyInAnyOrder("Second.User", username)
      } else {
        assertThat(result).noneMatch { it.assignedTo.username == assignedToOther.username }
      }
    }
  }

  @Nested
  inner class GetCases {

    private fun sortCase(
      crn: String,
      status: CaseAccommodationStatus,
      statusDate: LocalDate? = null,
      firstName: String = "First",
      lastName: String = "Last",
      hasCurrentAccommodation: Boolean = false,
    ) = SortCaseDefinition(
      crn = crn,
      status = status,
      statusDate = statusDate,
      firstName = firstName,
      lastName = lastName,
      hasCurrentAccommodation = hasCurrentAccommodation,
    )

    private fun assertCrnOrder(result: List<CaseDto>, vararg expectedCrns: String) {
      assertThat(result.map { it.crn }).containsExactly(*expectedCrns)
    }

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

    private fun setupSortScenario(vararg cases: SortCaseDefinition): List<PersonDto> {
      val staff = buildOfficer(username = username)
      val crnList = cases.map { it.crn }

      every { caseRepository.mapByCrns(crnList) } returns cases.associate { sortCase ->
        sortCase.crn to buildCaseEntity {
          withCrn(sortCase.crn)
          accommodationStatus = sortCase.status
          accommodationStatusDate = sortCase.statusDate
          firstName = sortCase.firstName
          lastName = sortCase.lastName
          if (sortCase.hasCurrentAccommodation) {
            currentAccommodation = buildAccommodationSummaryDto(crn = sortCase.crn)
          }
        }
      }

      return cases.map { buildFullPersonDto(crn = it.crn, staff = staff) }
    }

    private fun setupFourCaseScenario(): List<PersonDto> = setupSortScenario(
      sortCase(crn = crnOne, status = CaseAccommodationStatus.SETTLED, hasCurrentAccommodation = true),
      sortCase(crn = crnTwo, status = CaseAccommodationStatus.TRANSIENT),
      sortCase(crn = crnThree, status = CaseAccommodationStatus.RISK_OF_NO_FIXED_ABODE),
      sortCase(crn = crnFour, status = CaseAccommodationStatus.NO_FIXED_ABODE),
    )

    @BeforeEach
    fun setUp() {
      every { userService.getUsername() } returns Username(username)
    }

    @Test
    fun `CaseDto is redacted when UserAccess is Limited`() {
      every { caseRepository.mapByCrns(any()) } returns emptyMap()

      val result = caseQueryService.getCases(personDtos = personDtos)
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
      )
      val caseDto2 = personDto2.toCaseDto(caseEntity = caseEntity2, currentAccommodation = null, nextAccommodation = null)
      val caseDto3 = personDto3.toCaseDto(caseEntity = caseEntity3, currentAccommodation = null, nextAccommodation = null)
      every { caseRepository.mapByCrns(crnList) } returns caseEntities

      val result = caseQueryService.getCases(personDtos = personDtos)

      assertThat(result).hasSize(3)
      assertThat(result.map { it to it.limitedAccess to it.userAccess })
        .contains(
          caseDto1 to false to UserAccess.FULL,
          caseDto2 to false to UserAccess.FULL,
          caseDto3 to true to UserAccess.LIMITED,
        )
    }

    @Test
    fun `should get cases as all cases from case table and sort them`() {
      val personDtos = setupFourCaseScenario()

      val result = caseQueryService.getCases(personDtos = personDtos)

      assertThat(result).hasSize(4)

      assertThat(result.map { it.crn to it.accommodationSummaries?.caseAccommodationStatus })
        .containsExactly(
          crnThree to CaseAccommodationStatus.RISK_OF_NO_FIXED_ABODE,
          crnFour to CaseAccommodationStatus.NO_FIXED_ABODE,
          crnTwo to CaseAccommodationStatus.TRANSIENT,
          crnOne to CaseAccommodationStatus.SETTLED,
        )
    }

    @Test
    fun `should sort RISK_OF_NO_FIXED_ABODE by soonest status date, surname, firstname, then crn`() {
      val personDtos = setupSortScenario(
        sortCase(crn = crnFive, status = CaseAccommodationStatus.RISK_OF_NO_FIXED_ABODE, statusDate = today.plusDays(1), firstName = "Zulu", lastName = "Beta"),
        sortCase(crn = crnThree, status = CaseAccommodationStatus.RISK_OF_NO_FIXED_ABODE, statusDate = today.plusDays(1), firstName = "Beta", lastName = "Beta"),
        sortCase(crn = crnOne, status = CaseAccommodationStatus.RISK_OF_NO_FIXED_ABODE, statusDate = today, firstName = "Whisky", lastName = "Tango"),
        sortCase(crn = crnFour, status = CaseAccommodationStatus.RISK_OF_NO_FIXED_ABODE, statusDate = today.plusDays(1), firstName = "Zulu", lastName = "Beta"),
        sortCase(crn = crnTwo, status = CaseAccommodationStatus.RISK_OF_NO_FIXED_ABODE, statusDate = today.plusDays(1), firstName = "Zulu", lastName = "Alpha"),
      )

      val result = caseQueryService.getCases(personDtos = personDtos)

      assertCrnOrder(result, crnOne, crnTwo, crnThree, crnFour, crnFive)
    }

    @Test
    fun `should sort NO_FIXED_ABODE by oldest status date, surname, firstname, then crn`() {
      val personDtos = setupSortScenario(
        sortCase(crn = crnFour, status = CaseAccommodationStatus.NO_FIXED_ABODE, statusDate = today.minusDays(1), firstName = "Zulu", lastName = "Beta"),
        sortCase(crn = crnOne, status = CaseAccommodationStatus.NO_FIXED_ABODE, statusDate = today.minusDays(2), firstName = "Whisky", lastName = "Tango"),
        sortCase(crn = crnFive, status = CaseAccommodationStatus.NO_FIXED_ABODE, statusDate = today.minusDays(1), firstName = "Zulu", lastName = "Beta"),
        sortCase(crn = crnThree, status = CaseAccommodationStatus.NO_FIXED_ABODE, statusDate = today.minusDays(1), firstName = "Beta", lastName = "Beta"),
        sortCase(crn = crnTwo, status = CaseAccommodationStatus.NO_FIXED_ABODE, statusDate = today.minusDays(1), firstName = "Zulu", lastName = "Alpha"),
      )

      val result = caseQueryService.getCases(personDtos = personDtos)

      assertCrnOrder(result, crnOne, crnTwo, crnThree, crnFour, crnFive)
    }

    @ParameterizedTest
    @EnumSource(value = CaseAccommodationStatus::class, names = ["TRANSIENT", "SETTLED"])
    fun `should sort by oldest future status dates, then same rules but for past dates`(status: CaseAccommodationStatus) {
      val personDtos = setupSortScenario(
        sortCase(crn = crnSix, status = status, statusDate = today.minusDays(2), firstName = "CHARLIE", lastName = "CHARLIE"),
        sortCase(crn = crnEight, status = status, statusDate = today, firstName = "Alpha", lastName = "Alpha"),
        sortCase(crn = crnSeven, status = status, statusDate = today.minusDays(1), firstName = "Alpha", lastName = "Alpha"),
        sortCase(crn = crnThree, status = status, statusDate = today.plusDays(1), firstName = "Beta", lastName = "Beta"),
        sortCase(crn = crnFour, status = status, statusDate = today.plusDays(2), firstName = "Beta", lastName = "Beta"),
        sortCase(crn = crnOne, status = status, firstName = "Zulu", lastName = "Zulu"),
        sortCase(crn = crnFive, status = status, statusDate = today.minusDays(2), firstName = "beta", lastName = "beta"),
        sortCase(crn = crnTwo, status = status, statusDate = today.plusDays(1), firstName = "Alpha", lastName = "Beta"),
      )

      val result = caseQueryService.getCases(personDtos = personDtos)

      assertCrnOrder(result, crnOne, crnTwo, crnThree, crnFour, crnFive, crnSix, crnSeven, crnEight)
    }

    @Test
    fun `should sort each status group independently within the overall status ordering`() {
      val personDtos = setupSortScenario(
        sortCase(crn = crnEight, status = CaseAccommodationStatus.SETTLED, statusDate = today.plusDays(1), firstName = "Beta", lastName = "Beta"),
        sortCase(crn = crnSix, status = CaseAccommodationStatus.TRANSIENT, statusDate = today, firstName = "Alpha", lastName = "Alpha"),
        sortCase(crn = crnThree, status = CaseAccommodationStatus.NO_FIXED_ABODE, statusDate = today.minusDays(2), firstName = "Zulu", lastName = "Zulu"),
        sortCase(crn = crnSeven, status = CaseAccommodationStatus.SETTLED, firstName = "Zulu", lastName = "Zulu"),
        sortCase(crn = crnOne, status = CaseAccommodationStatus.RISK_OF_NO_FIXED_ABODE, statusDate = today, firstName = "Zulu", lastName = "Zulu"),
        sortCase(crn = crnNine, status = CaseAccommodationStatus.SETTLED, statusDate = today, firstName = "Alpha", lastName = "Alpha"),
        sortCase(crn = crnFour, status = CaseAccommodationStatus.NO_FIXED_ABODE, statusDate = today.minusDays(1), firstName = "Alpha", lastName = "Alpha"),
        sortCase(crn = crnTwo, status = CaseAccommodationStatus.RISK_OF_NO_FIXED_ABODE, statusDate = today.plusDays(1), firstName = "Alpha", lastName = "Alpha"),
        sortCase(crn = crnFive, status = CaseAccommodationStatus.TRANSIENT, firstName = "Zulu", lastName = "Zulu"),
      )

      val result = caseQueryService.getCases(personDtos = personDtos)

      assertCrnOrder(
        result,
        crnOne,
        crnTwo,
        crnThree,
        crnFour,
        crnFive,
        crnSix,
        crnSeven,
        crnEight,
        crnNine,
      )
    }

    @ParameterizedTest
    @CsvSource(
      value = ["HOUSED", "NFA_RISK", "<NULL>"],
      nullValues = ["<NULL>"],
    )
    fun `should get cases as all cases from case table and filter them`(peopleType: PeopleType?) {
      val personDtos = setupFourCaseScenario()

      val result = caseQueryService.getCases(personDtos = personDtos, peopleType = peopleType)

      when (peopleType) {
        PeopleType.HOUSED -> {
          assertThat(result).hasSize(1)
          assertThat(result.map { it.crn to it.accommodationSummaries?.caseAccommodationStatus })
            .containsExactly(
              crnOne to CaseAccommodationStatus.SETTLED,
            )
        }
        PeopleType.NFA_RISK -> {
          assertThat(result).hasSize(3)
          assertThat(result.map { it.crn to it.accommodationSummaries?.caseAccommodationStatus })
            .contains(
              crnThree to CaseAccommodationStatus.RISK_OF_NO_FIXED_ABODE,
              crnFour to CaseAccommodationStatus.NO_FIXED_ABODE,
              crnTwo to CaseAccommodationStatus.TRANSIENT,
            )
        }
        else -> {
          assertThat(result).hasSize(4)
          assertThat(result.map { it.crn to it.accommodationSummaries?.caseAccommodationStatus })
            .contains(
              crnThree to CaseAccommodationStatus.RISK_OF_NO_FIXED_ABODE,
              crnFour to CaseAccommodationStatus.NO_FIXED_ABODE,
              crnTwo to CaseAccommodationStatus.TRANSIENT,
              crnOne to CaseAccommodationStatus.SETTLED,
            )
        }
      }
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

      val person = toPersonDto(caseOrchestrationDto.case!!)

      val result = caseQueryService.getCase(crnOne)
      assertThat(result.data).isEqualTo(
        toCaseDto(
          crn = crnOne,
          person = person,
          cpr = caseOrchestrationDto.cpr,
          tier = caseOrchestrationDto.tier,
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

      assertThat(result.data).isEqualTo(toPersonDto(caseOrchestrationDto.case!!))
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
