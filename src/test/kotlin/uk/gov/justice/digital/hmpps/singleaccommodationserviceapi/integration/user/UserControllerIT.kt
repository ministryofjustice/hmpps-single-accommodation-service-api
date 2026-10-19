package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.user

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.client.expectBody
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.assertions.assertThatJson
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.config.GrantType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildNomisUserDetail
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildStaffDetail
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildTeam
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.USERNAME_OF_LOGGED_IN_DELIUS_USER
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.USERNAME_OF_LOGGED_IN_NOMIS_USER
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.HmppsAuthStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.NomisUserRolesStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.ProbationIntegrationDeliusStubs
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
    val result = restTestClient.get().uri("/user/teams")
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
          "teams": [
            {"name": "TEAMNAME1", "code": "TEAM1"},
            {"name": "TEAMNAME2", "code": "TEAM2"}
          ],
          "probationAreaCode": "PA123"
        }
      }
    """.trimIndent()
    val result = restTestClient.get().uri("/users/me")
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

    NomisUserRolesStubs.stubMe(
      jwt = jwt,
      response = buildNomisUserDetail(username = USERNAME_OF_LOGGED_IN_NOMIS_USER),
    )

    val expectedResponse = """{"data":{"username":"$USERNAME_OF_LOGGED_IN_NOMIS_USER"}}"""
    val result = restTestClient.get().uri("/users/me")
      .withNomisUserJwt(jwt = jwt)
      .exchangeSuccessfully()
      .expectBody<String>()
      .returnResult().responseBody!!

    assertThatJson(result).matchesExpectedJson(expectedResponse)
  }

  @Test
  fun `returns 401 when the delius staff record is not found during authentication`() {
    // does not create a delius user so that authentication has to look the staff record up
    HmppsAuthStubs.stubGrantToken()
    ProbationIntegrationDeliusStubs.stubGetStaffByUsernameNotFound(USERNAME_OF_LOGGED_IN_DELIUS_USER)

    restTestClient.get().uri("/user/teams")
      .withDeliusUserJwt()
      .exchange()
      .expectStatus().isUnauthorized
  }
}
