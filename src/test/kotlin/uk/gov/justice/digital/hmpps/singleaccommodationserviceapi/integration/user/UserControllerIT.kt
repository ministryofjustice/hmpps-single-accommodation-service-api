package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.user

import com.github.tomakehurst.wiremock.client.WireMock
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.assertions.assertThatJson
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.config.GrantType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildName
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildNomisUserDetail
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildStaffDetail
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildTeam
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildTeamStaffMember
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.USERNAME_OF_LOGGED_IN_DELIUS_USER
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.USERNAME_OF_LOGGED_IN_NOMIS_USER
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.HmppsAuthStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.NomisUserRolesStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.ProbationIntegrationDeliusStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.SasAndDeliusStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.WireMockManager.Companion.sasWiremock
import uk.gov.justice.hmpps.kotlin.auth.AuthSource

class UserControllerIT : IntegrationTestBase() {

  @Test
  fun `returns list of teams for a user`() {
    val deliusUser = createDeliusUser()
    HmppsAuthStubs.stubGrantToken()

    val teams = listOf(buildTeam(code = "TEAM1", name = "TEAMNAME1"), buildTeam(code = "TEAM2", name = "TEAMNAME2"))
    val staffDetail = buildStaffDetail(
      username = deliusUser.username,
      email = deliusUser.email!!,
      teams = teams,
    )

    ProbationIntegrationDeliusStubs.stubGetStaffByUsername(
      deliusUsername = USERNAME_OF_LOGGED_IN_DELIUS_USER,
      response = staffDetail,
    )

    val expectedResponse = """{"data":[{"name":"TEAMNAME1","code":"TEAM1"},{"name":"TEAMNAME2","code":"TEAM2"}]}"""
    val result = webTestClient.get().uri("/user/teams")
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody<String>()
      .returnResult().responseBody!!

    assertThatJson(result).matchesExpectedJson(expectedResponse)
  }

  @Test
  fun `returns delius user data including teams and probation area`() {
    val deliusUser = createDeliusUser()
    HmppsAuthStubs.stubGrantToken()

    val teams = listOf(buildTeam(code = "TEAM1", name = "TEAMNAME1"), buildTeam(code = "TEAM2", name = "TEAMNAME2"))
    val staffDetail = buildStaffDetail(
      username = deliusUser.username,
      email = deliusUser.email!!,
      teams = teams,
    )

    ProbationIntegrationDeliusStubs.stubGetStaffByUsername(
      deliusUsername = USERNAME_OF_LOGGED_IN_DELIUS_USER,
      response = staffDetail,
    )

    val expectedResponse = """
      {
        "data": {
          "username": "$USERNAME_OF_LOGGED_IN_DELIUS_USER",
          "userType": "DELIUS",
          "deliusUserDetails": {
            "teams": [
              {"name": "TEAMNAME1", "code": "TEAM1"},
              {"name": "TEAMNAME2", "code": "TEAM2"}
            ],
            "probationAreaCode": "PA123"
          }
        }
      }
    """.trimIndent()
    val result = webTestClient.get().uri("/users/me")
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody<String>()
      .returnResult().responseBody!!

    assertThatJson(result).matchesExpectedJson(expectedResponse)
  }

  @Disabled("Nomis users are not implemented yet")
  @Test
  fun `returns nomis user data`() {
    val jwt = jwtAuthHelper.createJwtAccessToken(
      grantType = GrantType.AUTHORIZATION_CODE.type,
      username = USERNAME_OF_LOGGED_IN_NOMIS_USER,
      roles = listOf("ROLE_POM", "ROLE_PRISON"),
      authSource = AuthSource.NOMIS.source,
    )

    NomisUserRolesStubs.stubNomisUserDetailsResponse(
      username = USERNAME_OF_LOGGED_IN_NOMIS_USER,
      response = buildNomisUserDetail(
        username = USERNAME_OF_LOGGED_IN_NOMIS_USER,
        activeCaseloadId = "MDI",
      ),
    )

    val expectedResponse = """
      {
        "data": {
          "username": "$USERNAME_OF_LOGGED_IN_NOMIS_USER",
          "userType": "NOMIS",
          "nomisUserDetails": {
            "prisonCode": "MDI"
          }
        }
      }
    """.trimIndent()
    val result = webTestClient.get().uri("/users/me")
      .withNomisUserJwt(jwt = jwt)
      .exchangeSuccessfully()
      .expectBody<String>()
      .returnResult().responseBody!!

    assertThatJson(result).matchesExpectedJson(expectedResponse)
  }

  @Test
  fun `returns the processed team members from all teams and pages`() {
    stubUserWithTeams("TEAM1", "TEAM2")
    SasAndDeliusStubs.stubGetStaffForTeams(
      teamCodes = listOf("TEAM1", "TEAM2"),
      staff = listOf(
        buildTeamStaffMember(username = "USER3", name = buildName(forename = "AForename", surname = "CSurname")),
        buildTeamStaffMember(username = "USER2", name = buildName(forename = "BForename", surname = "aSurname")),
        buildTeamStaffMember(username = null, name = buildName(forename = "NoUsername", surname = "ASurname")),
        buildTeamStaffMember(username = "USER1", name = buildName(forename = "AForename", surname = "ASurname")),
        buildTeamStaffMember(username = "USER3", name = buildName(forename = "AForename", surname = "CSurname")),
        buildTeamStaffMember(username = USERNAME_OF_LOGGED_IN_DELIUS_USER, name = buildName(forename = "DeliusForename", surname = "DeliusSurname")),
      ),
    )

    val expectedResponse = """
      {
        "data": [
          {"username": "USER1", "forename": "AForename", "surname": "ASurname"},
          {"username": "USER3", "forename": "AForename", "surname": "CSurname"},
          {"username": "USER2", "forename": "BForename", "surname": "aSurname"}
        ]
      }
    """.trimIndent()
    val result = webTestClient.get().uri("/users/me/team-members")
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody<String>()
      .returnResult().responseBody!!

    assertThatJson(result).matchesExpectedJson(expectedResponse)
    sasWiremock.verify(6, WireMock.getRequestedFor(WireMock.urlPathEqualTo("/team/staff")))
  }

  @Test
  fun `returns no team members without calling Delius when the user has no teams`() {
    stubUserWithTeams()

    webTestClient.get().uri("/users/me/team-members")
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody()
      .jsonPath("$.data.length()").isEqualTo(0)
      .jsonPath("$.upstreamFailures").doesNotExist()

    sasWiremock.verify(0, WireMock.getRequestedFor(WireMock.urlPathEqualTo("/team/staff")))
  }

  @Test
  fun `returns the team members it could fetch and an upstream failure when a later page fails`() {
    stubUserWithTeams("TEAM1")
    SasAndDeliusStubs.stubGetStaffForTeams(
      teamCodes = listOf("TEAM1"),
      staff = listOf(
        buildTeamStaffMember(username = "USER1", name = buildName(forename = "AForename", surname = "ASurname")),
        buildTeamStaffMember(username = "USER2", name = buildName(forename = "BForename", surname = "BSurname")),
      ),
    )
    SasAndDeliusStubs.stubGetStaffForTeamsFailure(page = 1)

    webTestClient.get().uri("/users/me/team-members")
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody()
      .jsonPath("$.data.length()").isEqualTo(1)
      .jsonPath("$.data[0].username").isEqualTo("USER1")
      .jsonPath("$.upstreamFailures.length()").isEqualTo(1)
      .jsonPath("$.upstreamFailures[0].endpoint").isEqualTo("getStaffForTeams1")
  }

  @Test
  fun `returns no team members and an upstream failure when the first page fails`() {
    stubUserWithTeams("TEAM1")
    SasAndDeliusStubs.stubGetStaffForTeamsFailure(page = 0)

    webTestClient.get().uri("/users/me/team-members")
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody()
      .jsonPath("$.data.length()").isEqualTo(0)
      .jsonPath("$.upstreamFailures.length()").isEqualTo(1)
      .jsonPath("$.upstreamFailures[0].endpoint").isEqualTo("getStaffForTeams0")
  }

  @Test
  fun `returns 401 when the delius staff record is not found during authentication`() {
    // does not create a delius user so that authentication has to look the staff record up
    HmppsAuthStubs.stubGrantToken()
    ProbationIntegrationDeliusStubs.stubGetStaffByUsernameNotFound(USERNAME_OF_LOGGED_IN_DELIUS_USER)

    webTestClient.get().uri("/user/teams")
      .withDeliusUserJwt()
      .exchange()
      .expectStatus().isUnauthorized
  }

  private fun stubUserWithTeams(vararg teamCodes: String) {
    val deliusUser = createDeliusUser()
    HmppsAuthStubs.stubGrantToken()
    ProbationIntegrationDeliusStubs.stubGetStaffByUsername(
      deliusUsername = USERNAME_OF_LOGGED_IN_DELIUS_USER,
      response = buildStaffDetail(
        username = deliusUser.username,
        email = deliusUser.email!!,
        teams = teamCodes.map { buildTeam(code = it, name = "TEAMNAME") },
      ),
    )
  }
}
