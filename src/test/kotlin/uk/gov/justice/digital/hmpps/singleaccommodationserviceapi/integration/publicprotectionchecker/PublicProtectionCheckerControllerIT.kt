package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.publicprotectionchecker

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.assertions.assertThatJson
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.UserEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.PublicProtectionCheckerRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.HmppsAuthStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.DatabaseUtils.SasTables

class PublicProtectionCheckerControllerIT : IntegrationTestBase() {

  @Autowired
  private lateinit var publicProtectionCheckerRepository: PublicProtectionCheckerRepository

  private lateinit var crn: String
  private lateinit var case: CaseEntity
  private lateinit var deliusUser: UserEntity

  private val requestBody = """
    {
      "supervisionRequired": true,
      "controlMeasuresSuitable": false,
      "practicalSupportSufficient": true
    }
  """.trimIndent()

  @BeforeEach
  fun setup() {
    databaseUtils.truncate(SasTables.PUBLIC_PROTECTION_CHECKER)
    case = caseRepository.save(buildCaseEntity())
    crn = case.caseIdentifiers.first().identifier
    HmppsAuthStubs.stubGrantToken()
    deliusUser = createTestDataSetupUserAndDeliusUser().second
  }

  @Test
  fun `should persist submitted answers and return public protection required`() {
    val result = webTestClient.post().uri("/public-protection-checker/{crn}", crn)
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(requestBody)
      .withDeliusUserJwt()
      .exchange()
      .expectStatus().isCreated
      .expectBody<String>()
      .returnResult().responseBody!!

    assertThatJson(result).matchesExpectedJson("""{ "publicProtectionRequired": true }""")

    val persisted = publicProtectionCheckerRepository.findAllBySasCaseId(case.id).single()
    assertThat(persisted.supervisionRequired).isTrue()
    assertThat(persisted.controlMeasuresSuitable).isFalse()
    assertThat(persisted.practicalSupportSufficient).isTrue()
  }

  @Test
  fun `should return public protection not required when all answers are false`() {
    val result = webTestClient.post().uri("/public-protection-checker/{crn}", crn)
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(
        """
        {
          "supervisionRequired": false,
          "controlMeasuresSuitable": false,
          "practicalSupportSufficient": false
        }
        """.trimIndent(),
      )
      .withDeliusUserJwt()
      .exchange()
      .expectStatus().isCreated
      .expectBody<String>()
      .returnResult().responseBody!!

    assertThatJson(result).matchesExpectedJson("""{ "publicProtectionRequired": false }""")
  }

  @Test
  fun `should return 404 when case does not exist`() {
    webTestClient.post().uri("/public-protection-checker/{crn}", "X999999")
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(requestBody)
      .withDeliusUserJwt()
      .exchange()
      .expectStatus().isNotFound

    assertThat(publicProtectionCheckerRepository.count()).isZero()
  }

  @Test
  fun `should return 403 when user does not have the required role`() {
    webTestClient.post().uri("/public-protection-checker/{crn}", crn)
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(requestBody)
      .withDeliusUserJwt(roles = listOf("ROLE_SOME_OTHER_ROLE"))
      .exchange()
      .expectStatus().isForbidden
  }
}
