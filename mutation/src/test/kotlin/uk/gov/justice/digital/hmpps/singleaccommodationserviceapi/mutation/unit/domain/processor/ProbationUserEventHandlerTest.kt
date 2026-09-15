package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.unit.domain.processor

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.IncomingHmppsDomainEventType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.SnsDomainEvent
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AuthSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.UserEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.UserRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.Username
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.InboxEventHandler
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.InboxEventHelper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.handler.ProbationUserEventHandler
import java.time.OffsetDateTime
import java.util.UUID

@ExtendWith(MockKExtension::class)
class ProbationUserEventHandlerTest {

  @RelaxedMockK
  private lateinit var userRepository: UserRepository

  @RelaxedMockK
  private lateinit var inboxEventHelper: InboxEventHelper

  @InjectMockKs
  private lateinit var probationUserEventHandler: ProbationUserEventHandler

  val fromUsername = "from.user"

  val toUsername = "new.user"

  val inboxEvent = InboxEventHandler.InboxEvent(
    id = UUID.randomUUID(),
    eventDetailUrl = "localhost",
    payload = "payload",
  )

  @Test
  fun `supports PROBATION_USER_USERNAME_CHANGED event type`() {
    val expectedEventTypes = setOf(
      IncomingHmppsDomainEventType.PROBATION_USER_USERNAME_CHANGED.typeName,
    )

    assertThat(probationUserEventHandler.supportedEventTypes()).containsExactlyElementsOf(expectedEventTypes)
  }

  @Test
  fun `partition key is fromUsername`() {
    every { inboxEventHelper.toDomainEvent(any()) } returns probationUsernameChangedEvent()

    assertThat(probationUserEventHandler.getPartitionKey(inboxEvent)).isEqualTo(fromUsername)
  }

  @Test
  fun `should ignore probation user update when no matching delius user exists`() {
    every { inboxEventHelper.toDomainEvent(any()) } returns probationUsernameChangedEvent()
    every { userRepository.findByUsernameAndAuthSource(Username(fromUsername), AuthSource.DELIUS) } returns null

    assertThat(probationUserEventHandler.handle(inboxEvent)).isEqualTo(InboxEventHandler.Result.IGNORED)
    verify(exactly = 1) { userRepository.findByUsernameAndAuthSource(Username(fromUsername), AuthSource.DELIUS) }
  }

  @Test
  fun `should process probation user update and set new username in uppercase`() {
    val existingUser = userEntity(username = fromUsername.uppercase())

    every { inboxEventHelper.toDomainEvent(any()) } returns probationUsernameChangedEvent()
    every { userRepository.findByUsernameAndAuthSource(Username(fromUsername), AuthSource.DELIUS) } returns existingUser

    assertThat(probationUserEventHandler.handle(inboxEvent)).isEqualTo(InboxEventHandler.Result.PROCESSED)
    assertThat(existingUser.username).isEqualTo(toUsername.uppercase())
  }

  private fun probationUsernameChangedEvent() = SnsDomainEvent(
    eventType = IncomingHmppsDomainEventType.PROBATION_USER_USERNAME_CHANGED.typeName,
    version = 1,
    occurredAt = OffsetDateTime.now(),
    additionalInformation = mapOf(
      "fromUsername" to fromUsername,
      "toUsername" to toUsername,
    ),
  )

  private fun userEntity(username: String) = UserEntity(
    id = UUID.randomUUID(),
    username = username,
    authSource = AuthSource.DELIUS,
    forename = "Test",
    middleNames = null,
    surname = "User",
    email = null,
    telephoneNumber = null,
    nomisStaffId = null,
    nomisAccountType = null,
    nomisActiveCaseloadId = null,
    isEnabled = true,
    isActive = true,
  )
}
