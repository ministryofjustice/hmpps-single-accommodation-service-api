package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.user

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremisesanddelius.ApprovedPremisesAndDeliusCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildStaffDetail
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildTeam
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.HttpAuthService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.UserPrincipal
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.Username
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.user.DeliusUserDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.user.NomisUserDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.user.TeamDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.user.UserQueryService
import uk.gov.justice.hmpps.kotlin.auth.AuthSource
import java.util.UUID

@ExtendWith(MockKExtension::class)
class UserQueryServiceTest {

  @MockK
  lateinit var httpAuthService: HttpAuthService

  @MockK
  lateinit var approvedPremisesAndDeliusCachingService: ApprovedPremisesAndDeliusCachingService

  lateinit var userQueryService: UserQueryService

  @BeforeEach
  fun setUp() {
    userQueryService = UserQueryService(
      httpAuthService = httpAuthService,
      approvedPremisesAndDeliusCachingService = approvedPremisesAndDeliusCachingService,
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

    assertThat(userQueryService.getUserData()).isEqualTo(
      DeliusUserDto(
        username = principal.username.value,
        teams = listOf(
          TeamDto(name = "Team 1", code = "TEAM1"),
          TeamDto(name = "Team 2", code = "TEAM2"),
        ),
        probationAreaCode = "PA123",
      ),
    )
  }

  @Test
  fun `getUserData returns nomis user data`() {
    val principal = principal(AuthSource.NOMIS)

    every { httpAuthService.getPrincipalOrThrow() } returns principal

    assertThat(userQueryService.getUserData()).isEqualTo(
      NomisUserDto(username = principal.username.value),
    )
    verify(exactly = 0) { approvedPremisesAndDeliusCachingService.getStaffDetail("nomis.user") }
  }

  private fun principal(authSource: AuthSource) = UserPrincipal(
    sasUserId = UUID.fromString("00000000-0000-0000-0000-000000000001"),
    username = Username(if (authSource == AuthSource.NOMIS) "nomis.user" else "delius.user"),
    authSource = authSource,
  )
}
