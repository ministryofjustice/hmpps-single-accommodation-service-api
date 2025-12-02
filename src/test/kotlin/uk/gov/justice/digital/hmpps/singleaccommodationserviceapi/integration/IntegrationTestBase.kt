package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.servlet.MockMvc
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.config.TestRedissonConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.ApprovedPremisesMockServer
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.CorePersonRecordMockServer
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.ProbationIntegrationDeliusMockServer
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.ProbationIntegrationOasysMockServer
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.TierMockServer
import uk.gov.justice.hmpps.test.kotlin.auth.JwtAuthorisationHelper

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestRedissonConfig::class)
abstract class IntegrationTestBase {

  @Autowired
  lateinit var mockMvc: MockMvc

  @Autowired
  protected lateinit var webTestClient: WebTestClient

  @Autowired
  protected lateinit var jwtAuthHelper: JwtAuthorisationHelper

  internal fun setAuthorisation(
    username: String? = "AUTH_ADM",
    roles: List<String> = listOf(),
    scopes: List<String> = listOf("read"),
  ): (HttpHeaders) -> Unit = jwtAuthHelper.setAuthorisationHeader(username = username, scope = scopes, roles = roles)

  protected fun stubPingWithResponse(status: Int) {
    hmppsAuth.stubHealthPing(status)
  }

  companion object {
    @JvmField
    val hmppsAuth = HmppsAuthMockServer()

    @JvmField
    internal val approvedPremisesMockServer = ApprovedPremisesMockServer()

    @JvmField
    internal val corePersonRecordMockServer = CorePersonRecordMockServer()

    @JvmField
    internal val probationIntegrationDeliusMockServer = ProbationIntegrationDeliusMockServer()

    @JvmField
    internal val probationIntegrationOasysMockServer = ProbationIntegrationOasysMockServer()

    @JvmField
    internal val tierMockServer = TierMockServer()

    @BeforeAll
    @JvmStatic
    fun startMocks() {
      hmppsAuth.start()
      approvedPremisesMockServer.start()
      corePersonRecordMockServer.start()
      probationIntegrationDeliusMockServer.start()
      probationIntegrationOasysMockServer.start()
      tierMockServer.start()
    }

    @AfterAll
    @JvmStatic
    fun stopMocks() {
      hmppsAuth.stop()
      approvedPremisesMockServer.stop()
      corePersonRecordMockServer.stop()
      probationIntegrationDeliusMockServer.stop()
      probationIntegrationOasysMockServer.stop()
      tierMockServer.stop()
    }

    @BeforeEach
    fun resetStubs() {
      hmppsAuth.resetAll()
      approvedPremisesMockServer.resetAll()
      corePersonRecordMockServer.resetAll()
      probationIntegrationDeliusMockServer.resetAll()
      probationIntegrationOasysMockServer.resetAll()
      tierMockServer.resetAll()
    }
  }
}
