package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.user

import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.assertions.assertThatJson
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildStaffDetail
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildTeam
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.USERNAME_OF_LOGGED_IN_DELIUS_USER
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.HmppsAuthStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.ProbationIntegrationDeliusStubs

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
      .expectBody(String::class.java)
      .returnResult().responseBody!!

    assertThatJson(result).matchesExpectedJson(expectedResponse)
  }
}
