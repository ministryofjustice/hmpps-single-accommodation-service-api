package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.servlet.MockMvc
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.config.TestMockConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.config.TestRedissonConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.AccommodationDataDomainMockServer
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.ApprovedPremisesMockServer
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.CorePersonRecordMockServer
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.PrisonerSearchMockServer
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.ProbationIntegrationDeliusMockServer
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.ProbationIntegrationOasysMockServer
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.TierMockServer
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.config.RulesConfig
import uk.gov.justice.hmpps.test.kotlin.auth.JwtAuthorisationHelper

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
@Import(value = [TestRedissonConfig::class, TestMockConfig::class, RulesConfig::class])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
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

  val hmppsAuth = HmppsAuthMockServer()

  internal val approvedPremisesMockServer = ApprovedPremisesMockServer()

  internal val corePersonRecordMockServer = CorePersonRecordMockServer()

  internal val probationIntegrationDeliusMockServer = ProbationIntegrationDeliusMockServer()

  internal val probationIntegrationOasysMockServer = ProbationIntegrationOasysMockServer()

  internal val tierMockServer = TierMockServer()

  internal val prisonerSearchMockServer = PrisonerSearchMockServer()

  internal val accommodationDataDomainMockServer = AccommodationDataDomainMockServer()

  @BeforeAll
  fun startMocks() {
    hmppsAuth.start()
    approvedPremisesMockServer.start()
    corePersonRecordMockServer.start()
    probationIntegrationDeliusMockServer.start()
    probationIntegrationOasysMockServer.start()
    tierMockServer.start()
    prisonerSearchMockServer.start()
  }

  @AfterAll
  fun stopMocks() {
    hmppsAuth.stop()
    approvedPremisesMockServer.stop()
    corePersonRecordMockServer.stop()
    probationIntegrationDeliusMockServer.stop()
    probationIntegrationOasysMockServer.stop()
    tierMockServer.stop()
    prisonerSearchMockServer.stop()
  }

  @BeforeEach
  fun resetStubs() {
    hmppsAuth.resetAll()
    approvedPremisesMockServer.resetAll()
    corePersonRecordMockServer.resetAll()
    probationIntegrationDeliusMockServer.resetAll()
    probationIntegrationOasysMockServer.resetAll()
    tierMockServer.resetAll()
    prisonerSearchMockServer.resetAll()
  }
}
