package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration

import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.client.RestTestClient
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.config.TestClockConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.config.TestJaversAuthProvider
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.config.TestJpaAuditorConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.config.GrantType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildPersonName
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildStaffDetail
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.UserEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.UserRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.ProbationIntegrationDeliusStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.WireMockInitializer
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.WireMockInitializer.Companion.sasWiremock
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.config.RulesConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.DatabaseUtils
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.messaging.TestSqsDomainEventListener
import uk.gov.justice.digital.hmpps.subjectaccessrequest.SarIntegrationTestHelperConfig
import uk.gov.justice.hmpps.kotlin.auth.AuthSource
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.test.kotlin.auth.JwtAuthorisationHelper
import java.time.Duration
import java.time.Duration.ofMillis
import java.time.Duration.ofSeconds
import java.time.Instant
import java.util.UUID
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AuthSource as AuthSourceEntity

const val USERNAME_OF_TEST_DATA_SETUP_USER = "TEST_DATA_SETUP_USER"
const val FORENAME_OF_TEST_DATA_SETUP_USER: String = "Test"
const val SURNAME_OF_TEST_DATA_SETUP_USER: String = "Data Setup User"
const val NAME_OF_TEST_DATA_SETUP_USER: String = "$FORENAME_OF_TEST_DATA_SETUP_USER $SURNAME_OF_TEST_DATA_SETUP_USER"

const val USERNAME_OF_LOGGED_IN_DELIUS_USER = "DELIUS_USER"
const val FORENAME_OF_LOGGED_IN_DELIUS_USER: String = "Delius"
const val SURNAME_OF_LOGGED_IN_DELIUS_USER: String = "User"
const val NAME_OF_LOGGED_IN_DELIUS_USER: String = "$FORENAME_OF_LOGGED_IN_DELIUS_USER $SURNAME_OF_LOGGED_IN_DELIUS_USER"

const val USERNAME_OF_DELIUS_SYNC_USER = "DELIUS_SYNC_USER"
const val FORENAME_OF_DELIUS_SYNC_USER: String = "nDelius"
const val SURNAME_OF_DELIUS_SYNC_USER: String = "user"

const val USERNAME_OF_LOGGED_IN_NOMIS_USER = "NOMIS_USER"

private const val NOW_DATE_STRING = "2026-05-20T15:22:17Z"

@AutoConfigureWebTestClient
@AutoConfigureRestTestClient
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
@Import(value = [RulesConfig::class, TestJpaAuditorConfig::class, TestJaversAuthProvider::class, TestClockConfig::class, SarIntegrationTestHelperConfig::class])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ContextConfiguration(initializers = [WireMockInitializer::class])
@Tag("integration")
abstract class IntegrationTestBase {
  protected val fixedInstant: Instant = Instant.parse(NOW_DATE_STRING)

  @Value($$"${test-data-setup.user-id}")
  protected lateinit var userIdOfTestDataSetupUser: UUID
  protected val userIdOfLoggedInDeliusUser: UUID = UUID.fromString("a1e2345f-b847-409a-9fee-aa75659bd9f8")
  protected val userIdOfDeliusSyncUser: UUID = UUID.fromString("dc403174-5986-4824-9783-09d33602ad9f")

  @Autowired
  lateinit var applicationContext: ApplicationContext

  @Autowired
  lateinit var restTestClient: RestTestClient

  @Autowired
  protected lateinit var jwtAuthHelper: JwtAuthorisationHelper

  @Autowired
  protected lateinit var userRepository: UserRepository

  @Autowired
  protected lateinit var databaseUtils: DatabaseUtils

  @Autowired
  lateinit var hmppsQueueService: HmppsQueueService

  @Autowired
  lateinit var testSqsDomainEventListener: TestSqsDomainEventListener

  @BeforeAll
  fun beforeAll() {
    await
      .atMost(ofSeconds(10))
      .pollInterval(ofMillis(100))
      .logging()
      .untilAsserted {
        restTestClient.get().uri("/health/readiness")
          .exchange()
          .expectStatus().isEqualTo(200)
          .expectBody().jsonPath("status").isEqualTo("UP")
      }
  }

  @BeforeEach
  fun resetStubs() {
    sasWiremock.resetAll()
    databaseUtils.truncate()
  }

  private val staffDetail = buildStaffDetail(
    username = USERNAME_OF_LOGGED_IN_DELIUS_USER,
    email = USERNAME_OF_LOGGED_IN_DELIUS_USER,
    name = buildPersonName(
      forename = FORENAME_OF_LOGGED_IN_DELIUS_USER,
      surname = SURNAME_OF_LOGGED_IN_DELIUS_USER,
    ),
    active = true,
  )

  protected fun createDeliusUser() = userRepository.save(delisUserEntityForJwtLoggedInUser()).also { user ->
    ProbationIntegrationDeliusStubs.stubGetStaffByUsername(user.username, staffDetail)
  }

  protected fun createDeliusSyncUser() = userRepository.save(deliusSyncUser())

  protected fun createTestDataSetupUserAndDeliusUser() = userRepository.save(testDataSetupUser()).also { user ->
    ProbationIntegrationDeliusStubs.stubGetStaffByUsername(user.username, staffDetail)
  } to createDeliusUser()

  protected fun testDataSetupUser() = UserEntity(
    id = userIdOfTestDataSetupUser,
    username = USERNAME_OF_TEST_DATA_SETUP_USER,
    authSource = AuthSourceEntity.DELIUS,
    forename = FORENAME_OF_TEST_DATA_SETUP_USER,
    middleNames = null,
    surname = SURNAME_OF_TEST_DATA_SETUP_USER,
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
    authSource = AuthSourceEntity.DELIUS,
    forename = FORENAME_OF_LOGGED_IN_DELIUS_USER,
    middleNames = null,
    surname = SURNAME_OF_LOGGED_IN_DELIUS_USER,
    email = USERNAME_OF_LOGGED_IN_DELIUS_USER,
    telephoneNumber = null,
    deliusStaffCode = null,
    nomisStaffId = null,
    nomisAccountType = null,
    nomisActiveCaseloadId = null,
    isEnabled = true,
    isActive = true,
  )

  protected fun deliusSyncUser() = UserEntity(
    id = userIdOfDeliusSyncUser,
    username = USERNAME_OF_DELIUS_SYNC_USER,
    authSource = AuthSourceEntity.DELIUS,
    forename = FORENAME_OF_DELIUS_SYNC_USER,
    middleNames = null,
    surname = SURNAME_OF_DELIUS_SYNC_USER,
    email = USERNAME_OF_DELIUS_SYNC_USER,
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
    roles: List<String> = listOf("SINGLE_ACCOMMODATION_SERVICE_PROBATION_PRACTITIONER"),
  ): RestTestClient.RequestHeadersSpec<*> = this.headers {
    it.setBearerAuth(
      jwtAuthHelper.createJwtAccessToken(
        grantType = GrantType.AUTHORIZATION_CODE.type,
        username = username,
        roles = roles,
        authSource = AuthSource.DELIUS.source,
      ),
    )
  }

  fun RestTestClient.RequestHeadersSpec<*>.withNomisUserJwt(
    username: String = USERNAME_OF_LOGGED_IN_NOMIS_USER,
    roles: List<String> = listOf("ROLE_POM", "ROLE_PRISON"),
    jwt: String = jwtAuthHelper.createJwtAccessToken(
      grantType = GrantType.AUTHORIZATION_CODE.type,
      username = username,
      roles = roles,
      authSource = AuthSource.NOMIS.source,
    ),
  ): RestTestClient.RequestHeadersSpec<*> = this.headers {
    it.setBearerAuth(jwt)
  }

  fun RestTestClient.RequestHeadersSpec<*>.withClientCredentialsJwt(
    roles: List<String>,
  ): RestTestClient.RequestHeadersSpec<*> = this.headers {
    it.setBearerAuth(
      jwtAuthHelper.createJwtAccessToken(
        grantType = GrantType.CLIENT_CREDENTIALS.type,
        clientId = "test-client-id",
        username = null,
        roles = roles,
        authSource = AuthSource.NONE.source,
      ),
    )
  }

  fun assertMessageReceived(
    typeName: String,
    eventDescription: String,
    detailUrl: String?,
    cprAddressId: UUID? = null,
  ): HmppsDomainEvent {
    var matchedMessage: HmppsDomainEvent? = null

    await.logging()
      .atMost(ofSeconds(5))
      .pollInterval(ofMillis(100))
      .untilAsserted {
        matchedMessage = testSqsDomainEventListener.takeMessageOrNull(typeName, eventDescription, detailUrl, cprAddressId)
        assertThat(matchedMessage).isNotNull()
      }
    return matchedMessage!!
  }
}
