package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.customcaselist

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseAccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.RiskLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.UserAccess
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationSummariesDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationaccesscontrol.CaseAccess
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationaccesscontrol.ProbationAccessControlService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationaccesscontrol.UserCaseAccess
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildUserCustomCaseListEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildUserEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withCrn
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withPrisonNumber
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.UserCustomCaseListRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.UserService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.customcaselist.CustomCaseListQueryService
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@ExtendWith(MockKExtension::class)
class CustomCaseListQueryServiceTest {
  private val clock: Clock = Clock.fixed(Instant.parse("2026-10-01T00:00:00Z"), ZoneOffset.UTC)

  @MockK
  lateinit var userService: UserService

  @MockK
  lateinit var caseRepository: CaseRepository

  @MockK
  lateinit var userCustomCaseListRepository: UserCustomCaseListRepository

  @MockK
  lateinit var probationAccessControlService: ProbationAccessControlService

  lateinit var customCaseListQueryService: CustomCaseListQueryService

  private val user = buildUserEntity(username = "DELIUS_USER")

  @BeforeEach
  fun setUp() {
    customCaseListQueryService = CustomCaseListQueryService(
      userService = userService,
      caseRepository = caseRepository,
      userCustomCaseListRepository = userCustomCaseListRepository,
      probationAccessControlService = probationAccessControlService,
      clock = clock,
    )
    every { userService.authorizeAndRetrieveUser() } returns user
  }

  @Test
  fun `returns an empty list without loading cases or checking access when the user has no custom case list`() {
    every { userCustomCaseListRepository.findAllBySasUserId(user.id) } returns emptyList()

    val result = customCaseListQueryService.getCustomCaseList()

    assertThat(result.data).isEmpty()
    verify(exactly = 0) { caseRepository.findAllWithIdentifiersByIdIn(any()) }
    verify(exactly = 0) { probationAccessControlService.getUserAccess(any(), any()) }
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
      accommodationSummariesDto = accommodationSummaries,
    ) {
      withCrn("A123456")
      withPrisonNumber("A1234AA")
    }
    stubCases(caseEntity)
    stubAccess(accessible = listOf("A123456"))

    val caseDto = customCaseListQueryService.getCustomCaseList().data.single()

    assertThat(caseDto.crn).isEqualTo("A123456")
    assertThat(caseDto.prisonNumber).isEqualTo("A1234AA")
    assertThat(caseDto.forename).isEqualTo("Joe")
    assertThat(caseDto.surname).isEqualTo("Bloggs")
    assertThat(caseDto.dateOfBirth).isEqualTo(LocalDate.of(1990, 1, 2))
    assertThat(caseDto.tierScore).isEqualTo("B2")
    assertThat(caseDto.riskLevel).isEqualTo(RiskLevel.HIGH)
    assertThat(caseDto.userAccess).isEqualTo(UserAccess.FULL)
    assertThat(caseDto.limitedAccess).isNull()
    assertThat(caseDto.assignedTo).isNull()
    assertThat(caseDto.pncReference).isNull()
    assertThat(caseDto.accommodationSummaries).isEqualTo(accommodationSummaries)
  }

  @Test
  fun `checks access for the logged in user against every crn in the custom case list`() {
    stubCases(buildCaseEntity { withCrn("A123456") }, buildCaseEntity { withCrn("B654321") })
    stubAccess(accessible = listOf("A123456", "B654321"))

    customCaseListQueryService.getCustomCaseList()

    verify { probationAccessControlService.getUserAccess("DELIUS_USER", match { it.toSet() == setOf("A123456", "B654321") }) }
  }

  @Test
  fun `returns cases the user is excluded or restricted from as limited cases with only the crn`() {
    stubCases(
      buildCaseEntity(firstName = "Joe", lastName = "Bloggs") { withCrn("A123456") },
      buildCaseEntity(firstName = "Jane", lastName = "Doe") { withCrn("B654321") },
      buildCaseEntity(firstName = "John", lastName = "Smith") { withCrn("C111111") },
    )
    stubAccess(accessible = listOf("A123456"), excluded = listOf("B654321"), restricted = listOf("C111111"))

    val result = customCaseListQueryService.getCustomCaseList()

    val limitedCases = result.data.filter { it.crn in listOf("B654321", "C111111") }
    assertThat(limitedCases).containsExactlyInAnyOrder(
      CaseDto(crn = "B654321", userAccess = UserAccess.LIMITED, limitedAccess = true),
      CaseDto(crn = "C111111", userAccess = UserAccess.LIMITED, limitedAccess = true),
    )
    assertThat(result.data.single { it.crn == "A123456" }.userAccess).isEqualTo(UserAccess.FULL)
  }

  @Test
  fun `returns cases missing from the access response as limited cases`() {
    stubCases(buildCaseEntity { withCrn("A123456") }, buildCaseEntity { withCrn("B654321") })
    stubAccess(accessible = listOf("A123456"))

    val result = customCaseListQueryService.getCustomCaseList()

    assertThat(result.data.single { it.crn == "B654321" })
      .isEqualTo(CaseDto(crn = "B654321", userAccess = UserAccess.LIMITED, limitedAccess = true))
  }

  @Test
  fun `sorts limited cases to the top followed by accessible cases in the standard case list ordering`() {
    stubCases(
      buildCaseEntity(lastName = "Settled", accommodationSummariesDto = buildAccommodationSummariesDto(CaseAccommodationStatus.SETTLED)) { withCrn("A111111") },
      buildCaseEntity(lastName = "Risk", accommodationSummariesDto = buildAccommodationSummariesDto(CaseAccommodationStatus.RISK_OF_NO_FIXED_ABODE)) { withCrn("B222222") },
      buildCaseEntity { withCrn("C333333") },
      buildCaseEntity { withCrn("D444444") },
    )
    stubAccess(accessible = listOf("A111111", "B222222"), excluded = listOf("D444444"), restricted = listOf("C333333"))

    val result = customCaseListQueryService.getCustomCaseList()

    assertThat(result.data.map { it.crn }).containsExactly("C333333", "D444444", "B222222", "A111111")
  }

  @Test
  fun `includes partially populated cases with blank fields in the standard case list ordering`() {
    val partial = buildCaseEntity(tierScore = null, firstName = "Aaron", lastName = "Aardvark") { withCrn("A111111") }
    val populated = buildCaseEntity(firstName = "Zed", lastName = "Zebra") { withCrn("B222222") }
    stubCases(populated, partial)
    stubAccess(accessible = listOf("A111111", "B222222"))

    val result = customCaseListQueryService.getCustomCaseList()

    assertThat(result.data.map { it.crn }).containsExactly("A111111", "B222222")
    assertThat(result.data.first().tierScore).isNull()
  }

  @Test
  fun `sorts refreshed cases using the standard case list ordering`() {
    stubCases(
      buildCaseEntity(lastName = "Settled", accommodationSummariesDto = buildAccommodationSummariesDto(CaseAccommodationStatus.SETTLED)) { withCrn("A111111") },
      buildCaseEntity(lastName = "Risk", accommodationSummariesDto = buildAccommodationSummariesDto(CaseAccommodationStatus.RISK_OF_NO_FIXED_ABODE)) { withCrn("B222222") },
      buildCaseEntity(lastName = "None", accommodationSummariesDto = null) { withCrn("C333333") },
      buildCaseEntity(lastName = "Nfa", accommodationSummariesDto = buildAccommodationSummariesDto(CaseAccommodationStatus.NO_FIXED_ABODE)) { withCrn("D444444") },
    )
    stubAccess(accessible = listOf("A111111", "B222222", "C333333", "D444444"))

    val result = customCaseListQueryService.getCustomCaseList()

    assertThat(result.data.map { it.crn }).containsExactly("C333333", "B222222", "D444444", "A111111")
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
  ) {
    every { probationAccessControlService.getUserAccess(user.username, any()) } returns UserCaseAccess(
      accessible.map { CaseAccess(crn = it, userExcluded = false, userRestricted = false) } +
        excluded.map { CaseAccess(crn = it, userExcluded = true, userRestricted = false) } +
        restricted.map { CaseAccess(crn = it, userExcluded = false, userRestricted = true) },
    )
  }
}
