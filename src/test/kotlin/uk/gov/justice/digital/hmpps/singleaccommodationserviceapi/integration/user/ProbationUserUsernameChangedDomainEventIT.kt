package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.user

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.context.TestPropertySource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildUserEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.IncomingHmppsDomainEventType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AuthSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProcessedStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.Username
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.DatabaseUtils

@TestPropertySource(properties = ["scheduling.enabled=true"])
class ProbationUserUsernameChangedDomainEventIT : IntegrationTestBase() {

  private val eventType = IncomingHmppsDomainEventType.PROBATION_USER_USERNAME_CHANGED
  private val fromUsername = "from.user"
  private val toUsername = "to.user"

  @BeforeEach
  fun setup() {
    testSqsDomainEventListener.assertQueueIsEmpty()
    databaseUtils.truncate(DatabaseUtils.SasTables.SAS_USER, DatabaseUtils.SasTables.INBOX_EVENT)
    createSasSystemUser()
  }

  @Test
  fun `should process incoming probation username changed domain event when matching DELIUS user exists`() {
    userRepository.save(buildUserEntity(username = fromUsername.uppercase(), authSource = AuthSource.DELIUS))

    publishProbationUsernameChangedEvent()

    testInboxEventHelper.assertExpectedInboxEvents(ProcessedStatus.PROCESSED, 1)

    val updatedDeliusUser = userRepository.findByUsernameAndAuthSource(Username(toUsername), AuthSource.DELIUS)
    assertThat(updatedDeliusUser).isNotNull
    assertThat(updatedDeliusUser!!.username).isEqualTo(toUsername.uppercase())

    val previousDeliusUsername = userRepository.findByUsernameAndAuthSource(Username(fromUsername), AuthSource.DELIUS)
    assertThat(previousDeliusUsername).isNull()
  }

  @Test
  fun `should ignore incoming probation username changed domain event when DELIUS user is not known`() {
    publishProbationUsernameChangedEvent()
    testInboxEventHelper.assertExpectedInboxEvents(ProcessedStatus.IGNORED, 1)
  }

  @Test
  fun `should ignore incoming probation username changed domain event when auth source is NOMIS`() {
    userRepository.save(buildUserEntity(username = fromUsername.uppercase(), authSource = AuthSource.NOMIS))

    publishProbationUsernameChangedEvent()

    testInboxEventHelper.assertExpectedInboxEvents(ProcessedStatus.IGNORED, 1)
    val nomisUser = userRepository.findByUsernameAndAuthSource(Username(fromUsername), AuthSource.NOMIS)
    assertThat(nomisUser).isNotNull
    assertThat(nomisUser!!.username).isEqualTo(fromUsername.uppercase())
  }

  @Test
  fun `should fail incoming probation username changed domain event when target DELIUS username already exists`() {
    val existingTargetUsername = "already.exists"

    userRepository.save(buildUserEntity(username = fromUsername.uppercase(), authSource = AuthSource.DELIUS))
    userRepository.save(buildUserEntity(username = existingTargetUsername.uppercase(), authSource = AuthSource.DELIUS))

    testInboxEventHelper.publish(
      messageType = eventType,
      crn = null,
      detailUrl = null,
      additionalInformation = mapOf(
        "fromUsername" to fromUsername,
        "toUsername" to existingTargetUsername,
      ),
    )

    testInboxEventHelper.assertExpectedInboxEvents(ProcessedStatus.FAILED, 1)

    val originalUser = userRepository.findByUsernameAndAuthSource(Username(fromUsername), AuthSource.DELIUS)
    assertThat(originalUser).isNotNull
    assertThat(originalUser!!.username).isEqualTo(fromUsername.uppercase())
  }

  private fun publishProbationUsernameChangedEvent() {
    testInboxEventHelper.publish(
      messageType = eventType,
      crn = null,
      detailUrl = null,
      additionalInformation = mapOf(
        "fromUsername" to fromUsername,
        "toUsername" to toUsername,
      ),
    )
  }
}
