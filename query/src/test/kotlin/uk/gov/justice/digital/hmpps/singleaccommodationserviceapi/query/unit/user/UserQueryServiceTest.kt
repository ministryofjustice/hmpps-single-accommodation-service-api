package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.user

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.OrchestrationResultDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremisesanddelius.ApprovedPremisesAndDeliusCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.nomisuserroles.NomisUserRolesService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildName
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildStaffDetail
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildTeam
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildTeamStaffMember
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.HttpAuthService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.UserPrincipal
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.Username
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildUpstreamFailure
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.user.DeliusUserDetailsDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.user.TeamDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.user.TeamMemberDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.user.UserDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.user.UserOrchestrationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.user.UserQueryService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.user.UserType
import uk.gov.justice.hmpps.kotlin.auth.AuthSource
import java.util.UUID

@ExtendWith(MockKExtension::class)
class UserQueryServiceTest {

  @MockK
  lateinit var httpAuthService: HttpAuthService

  @MockK
  lateinit var approvedPremisesAndDeliusCachingService: ApprovedPremisesAndDeliusCachingService

  @MockK
  lateinit var nomisUserRolesService: NomisUserRolesService

  @MockK
  lateinit var userOrchestrationService: UserOrchestrationService

  lateinit var userQueryService: UserQueryService

  @BeforeEach
  fun setUp() {
    userQueryService = UserQueryService(
      httpAuthService = httpAuthService,
      approvedPremisesAndDeliusCachingService = approvedPremisesAndDeliusCachingService,
      nomisUserRolesService = nomisUserRolesService,
      userOrchestrationService = userOrchestrationService,
    )
  }

  @Test
  fun `getTeams maps Delius teams into user team dto`() {
    val principal = principal(AuthSource.DELIUS)
    val teams = listOf(
      buildTeam(code = "TEAM1", name = "Team 1"),
      buildTeam(code = "TEAM2", name = "Team 2"),
    )

    every { httpAuthService.getPrincipalOrThrow() } returns principal
    every { approvedPremisesAndDeliusCachingService.getStaffDetail(principal.username.value) } returns buildStaffDetail(
      username = principal.username.value,
      teams = teams,
    )

    assertThat(userQueryService.getTeams()).isEqualTo(
      listOf(
        TeamDto(name = "Team 1", code = "TEAM1"),
        TeamDto(name = "Team 2", code = "TEAM2"),
      ),
    )
  }

  @Test
  fun `getUserData returns delius user data with teams and probation area`() {
    val principal = principal(AuthSource.DELIUS)
    val teams = listOf(
      buildTeam(code = "TEAM1", name = "Team 1"),
      buildTeam(code = "TEAM2", name = "Team 2"),
    )

    every { httpAuthService.getPrincipalOrThrow() } returns principal
    every { approvedPremisesAndDeliusCachingService.getStaffDetail(principal.username.value) } returns buildStaffDetail(
      username = principal.username.value,
      teams = teams,
    )

    val result = userQueryService.getUserData()

    assertThat(result).isEqualTo(
      UserDto(
        userType = UserType.DELIUS,
        username = principal.username.value,
        deliusUserDetails = DeliusUserDetailsDto(
          teams = listOf(
            TeamDto(name = "Team 1", code = "TEAM1"),
            TeamDto(name = "Team 2", code = "TEAM2"),
          ),
          probationAreaCode = "PA123",
        ),
      ),
    )
  }

  @Test
  fun `getUserData throws for unsupported auth source`() {
    val principal = principal(AuthSource.NONE)

    every { httpAuthService.getPrincipalOrThrow() } returns principal

    assertThatThrownBy { userQueryService.getUserData() }
      .isInstanceOf(IllegalStateException::class.java)
      .hasMessage("Could not find DELIUS or NOMIS auth source")
  }

  @Test
  fun `getUserTeamMembers returns staff from all teams sorted by forename then surname ignoring case`() {
    stubDeliusUserWithTeams("TEAM1", "TEAM2")
    every { userOrchestrationService.getStaffForTeams(listOf("TEAM1", "TEAM2")) } returns OrchestrationResultDto(
      data = listOf(
        buildTeamStaffMember(username = "USER3", name = buildName(forename = "BForename", surname = "ASurname")),
        buildTeamStaffMember(username = "USER2", name = buildName(forename = "aForename", surname = "CSurname")),
        buildTeamStaffMember(username = "USER1", name = buildName(forename = "AForename", surname = "bSurname")),
      ),
    )

    val result = userQueryService.getUserTeamMembers()

    assertThat(result.data).containsExactly(
      TeamMemberDto(username = "USER1", forename = "AForename", surname = "bSurname"),
      TeamMemberDto(username = "USER2", forename = "aForename", surname = "CSurname"),
      TeamMemberDto(username = "USER3", forename = "BForename", surname = "ASurname"),
    )
    assertThat(result.upstreamFailures).isEmpty()
  }

  @Test
  fun `getUserTeamMembers returns staff in more than one team once`() {
    stubDeliusUserWithTeams("TEAM1", "TEAM2")
    every { userOrchestrationService.getStaffForTeams(listOf("TEAM1", "TEAM2")) } returns OrchestrationResultDto(
      data = listOf(buildTeamStaffMember(username = "USER1"), buildTeamStaffMember(username = "USER1")),
    )

    assertThat(userQueryService.getUserTeamMembers().data.map { it.username }).containsExactly("USER1")
  }

  @Test
  fun `getUserTeamMembers returns usernames differing only by case once`() {
    stubDeliusUserWithTeams("TEAM1")
    every { userOrchestrationService.getStaffForTeams(listOf("TEAM1")) } returns OrchestrationResultDto(
      data = listOf(buildTeamStaffMember(username = "user1"), buildTeamStaffMember(username = "USER1")),
    )

    assertThat(userQueryService.getUserTeamMembers().data.map { it.username }).containsExactly("user1")
  }

  @Test
  fun `getUserTeamMembers omits staff without a username`() {
    stubDeliusUserWithTeams("TEAM1")
    every { userOrchestrationService.getStaffForTeams(listOf("TEAM1")) } returns OrchestrationResultDto(
      data = listOf(
        buildTeamStaffMember(username = null),
        buildTeamStaffMember(username = " "),
        buildTeamStaffMember(username = "USER1"),
      ),
    )

    assertThat(userQueryService.getUserTeamMembers().data.map { it.username }).containsExactly("USER1")
  }

  @Test
  fun `getUserTeamMembers omits the current user ignoring case`() {
    stubDeliusUserWithTeams("TEAM1")
    every { userOrchestrationService.getStaffForTeams(listOf("TEAM1")) } returns OrchestrationResultDto(
      data = listOf(
        buildTeamStaffMember(username = "DELIUS.USER"),
        buildTeamStaffMember(username = "USER1"),
      ),
    )

    assertThat(userQueryService.getUserTeamMembers().data.map { it.username }).containsExactly("USER1")
  }

  @Test
  fun `getUserTeamMembers returns upstream failures alongside the staff it could fetch`() {
    stubDeliusUserWithTeams("TEAM1")
    every { userOrchestrationService.getStaffForTeams(listOf("TEAM1")) } returns OrchestrationResultDto(
      data = listOf(buildTeamStaffMember(username = "USER1")),
      upstreamFailures = listOf(buildUpstreamFailure(callKey = "getStaffForTeams1")),
    )

    val result = userQueryService.getUserTeamMembers()

    assertThat(result.data.map { it.username }).containsExactly("USER1")
    assertThat(result.upstreamFailures.map { it.endpoint }).containsExactly("getStaffForTeams1")
  }

  @Test
  fun `getUserTeamMembers does not call Delius when the user has no teams`() {
    stubDeliusUserWithTeams()

    val result = userQueryService.getUserTeamMembers()

    assertThat(result.data).isEmpty()
    verify(exactly = 0) { userOrchestrationService.getStaffForTeams(any()) }
  }

  private fun stubDeliusUserWithTeams(vararg teamCodes: String) {
    val principal = principal(AuthSource.DELIUS)

    every { httpAuthService.getPrincipalOrThrow() } returns principal
    every { approvedPremisesAndDeliusCachingService.getStaffDetail(principal.username.value) } returns buildStaffDetail(
      username = principal.username.value,
      teams = teamCodes.map { buildTeam(code = it) },
    )
  }

  private fun principal(authSource: AuthSource) = UserPrincipal(
    sasUserId = UUID.fromString("00000000-0000-0000-0000-000000000001"),
    username = Username(if (authSource == AuthSource.NOMIS) "nomis.user" else "delius.user"),
    authSource = authSource,
  )
}
