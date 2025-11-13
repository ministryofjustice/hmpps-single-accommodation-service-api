package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.TestInfo
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.http.HttpHeaders
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.config.TestPropertiesInitializer
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.HmppsAuthApiExtension.Companion.hmppsAuth
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.WiremockManager
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.model.GetTokenResponse
import uk.gov.justice.hmpps.test.kotlin.auth.JwtAuthorisationHelper
import java.time.Duration
import java.util.TimeZone
import java.util.UUID

// @ExtendWith(HmppsAuthApiExtension::class)
@ContextConfiguration(initializers = [TestPropertiesInitializer::class])
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
@Tag("integration")
abstract class IntegrationTestBase {

  private val log = LoggerFactory.getLogger(this::class.java)

  @Autowired
  lateinit var objectMapper: ObjectMapper

  @Autowired
  protected lateinit var webTestClient: WebTestClient

  @Autowired
  protected lateinit var jwtAuthHelper: JwtAuthorisationHelper

  @Autowired
  lateinit var wiremockManager: WiremockManager

  val wiremockServer: WireMockServer by lazy {
    wiremockManager.wiremockServer
  }

  @BeforeEach
  fun beforeEach(info: TestInfo) {
    log.info("Running test '${info.displayName}'")

    if (!info.tags.contains("isPerClass")) {
      this.setupTests()
    }
  }

  @AfterEach
  fun afterEach(info: TestInfo) {
    if (!info.tags.contains("isPerClass")) {
      this.teardownTests()
    }
  }

  fun setupTests() {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

    webTestClient = webTestClient.mutate()
      .responseTimeout(Duration.ofMinutes(20))
      .build()

    wiremockManager.beforeTest()

    // TODO clear cache and redis when needed.
  }

  fun teardownTests() {
    wiremockManager.afterTest()
  }

  internal fun setAuthorisation(
    username: String? = "AUTH_ADM",
    roles: List<String> = listOf(),
    scopes: List<String> = listOf("read"),
  ): (HttpHeaders) -> Unit = jwtAuthHelper.setAuthorisationHeader(username = username, scope = scopes, roles = roles)

  protected fun stubPingWithResponse(status: Int) {
    hmppsAuth.stubHealthPing(status)
  }

  fun mockClientCredentialsJwtRequest(
    username: String? = null,
    roles: List<String> = listOf(),
    authSource: String = "none",
  ) {
    wiremockServer.stubFor(
      post(urlEqualTo("/auth/oauth/token"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(
              objectMapper.writeValueAsString(
                GetTokenResponse(
                  accessToken = jwtAuthHelper.createJwtAccessToken(
                    username = username,
                    roles = roles,
                    authSource = authSource,
                  ),
                  tokenType = "bearer",
                  expiresIn = Duration.ofHours(1).toSeconds().toInt(),
                  scope = "read",
                  sub = username?.uppercase() ?: "integration-test-client-id",
                  authSource = authSource,
                  jti = UUID.randomUUID().toString(),
                  iss = "http://localhost:9092/auth/issuer",
                ),
              ),
            ),
        ),
    )
  }
}
