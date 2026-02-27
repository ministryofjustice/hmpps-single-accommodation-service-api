package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration

import org.awaitility.kotlin.await
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.client.RestTestClient
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.config.TestJpaAuditorConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AuthSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.UserEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.UserRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.WireMockInitializer
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.config.RulesConfig
import uk.gov.justice.hmpps.test.kotlin.auth.JwtAuthorisationHelper
import java.time.Duration
import java.util.UUID

const val USERNAME_OF_TEST_DATA_SETUP_USER = "TEST_DATA_SETUP_USER"
const val NAME_OF_TEST_DATA_SETUP_USER: String = "Test Data Setup User"
const val USERNAME_OF_LOGGED_IN_DELIUS_USER = "DELIUS_USER"
const val NAME_OF_LOGGED_IN_DELIUS_USER: String = "DeliusUser"
const val USERNAME_OF_LOGGED_IN_NOMIS_USER = "NOMIS_USER"
const val NAME_OF_LOGGED_IN_NOMIS_USER: String = "NomisUser"

@AutoConfigureRestTestClient
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
@Import(value = [RulesConfig::class, TestJpaAuditorConfig::class])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ContextConfiguration(initializers = [WireMockInitializer::class])
@Tag("integration")
abstract class IntegrationTestBase {
  @Value($$"${test-data-setup.user-id}")
  protected lateinit var userIdOfTestDataSetupUser: UUID
  protected val userIdOfLoggedInDeliusUser: UUID = UUID.fromString("a1e2345f-b847-409a-9fee-aa75659bd9f8")

  @Autowired
  lateinit var applicationContext: ApplicationContext

  @Autowired
  lateinit var restTestClient: RestTestClient

  @Autowired
  protected lateinit var jwtAuthHelper: JwtAuthorisationHelper

  @Autowired
  protected lateinit var userRepository: UserRepository

  internal fun setAuthorisation(
    username: String? = "AUTH_ADM",
    roles: List<String> = listOf(),
    scopes: List<String> = listOf("read"),
  ): (HttpHeaders) -> Unit = jwtAuthHelper.setAuthorisationHeader(username = username, scope = scopes, roles = roles)

  @BeforeEach
  fun resetStubs() {
    userRepository.deleteAll()
  }

  @AfterEach
  fun teardownUsers() {
    userRepository.deleteAll()
  }

  protected fun createTestDataSetupUserAndDeliusUser() {
    userRepository.save(testDataSetupUser())
    userRepository.save(delisUserEntityForJwtLoggedInUser())
  }

  protected fun testDataSetupUser() = UserEntity(
    id = userIdOfTestDataSetupUser,
    username = USERNAME_OF_TEST_DATA_SETUP_USER,
    authSource = AuthSource.DELIUS,
    name = NAME_OF_TEST_DATA_SETUP_USER,
    email = USERNAME_OF_TEST_DATA_SETUP_USER,
    telephoneNumber = null,
    deliusStaffCode = null,
    nomisStaffId = null,
    nomisAccountType = null,
    nomisActiveCaseloadId = null,
    isEnabled = false,
    isActive = false,
  )

  protected fun delisUserEntityForJwtLoggedInUser() = UserEntity(
    id = userIdOfLoggedInDeliusUser,
    username = USERNAME_OF_LOGGED_IN_DELIUS_USER,
    authSource = AuthSource.DELIUS,
    name = NAME_OF_LOGGED_IN_DELIUS_USER,
    email = USERNAME_OF_LOGGED_IN_DELIUS_USER,
    telephoneNumber = null,
    deliusStaffCode = null,
    nomisStaffId = null,
    nomisAccountType = null,
    nomisActiveCaseloadId = null,
    isEnabled = true,
    isActive = true,
  )

  fun waitFor(pollInterval: Duration? = Duration.ofMillis(50), block: () -> Unit) {
    await.atMost(Duration.ofSeconds(10))
      .pollInterval(pollInterval)
      .untilAsserted(block)
  }

  fun <T> waitForEntity(
    pollInterval: Duration = Duration.ofMillis(50),
    timeout: Duration = Duration.ofSeconds(10),
    supplier: () -> T?,
  ): T = await
    .atMost(timeout)
    .pollInterval(pollInterval)
    .until({ supplier() }, { it != null })!!

  fun RestTestClient.RequestHeadersSpec<*>.withDeliusUserJwt(
    username: String = USERNAME_OF_LOGGED_IN_DELIUS_USER,
    roles: List<String> = listOf("ROLE_PROBATION"),
  ): RestTestClient.RequestHeadersSpec<*> = this.headers {
    it.setBearerAuth(
      jwtAuthHelper.createJwtAccessToken(
        username = username,
        roles = roles,
        authSource = AuthSource.DELIUS.value,
      ),
    )
  }

  fun RestTestClient.RequestHeadersSpec<*>.withNomisUserJwt(
    username: String = USERNAME_OF_LOGGED_IN_NOMIS_USER,
    roles: List<String> = listOf("ROLE_POM", "ROLE_PRISON"),
    jwt: String = jwtAuthHelper.createJwtAccessToken(
      username = username,
      roles = roles,
      authSource = AuthSource.NOMIS.value,
      // todo: figure out a way to add userUuid to jwt claims
    ),
  ): RestTestClient.RequestHeadersSpec<*> = this.headers {
    it.setBearerAuth(jwt)
  }
}
