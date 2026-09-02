package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.handler

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.IncomingHmppsDomainEventType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AuthSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.UserRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.Username
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.InboxEventHandler
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.InboxEventHelper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor.getAdditionalInformation

@Component
class ProbationUserEventHandler(
  private val userRepository: UserRepository,
  private val inboxEventHelper: InboxEventHelper,
) : InboxEventHandler {

  private val log = LoggerFactory.getLogger(javaClass)

  override fun supportedEventTypes() = setOf(
    IncomingHmppsDomainEventType.PROBATION_USER_USERNAME_CHANGED.typeName,
  )

  override fun getPartitionKey(inboxEvent: InboxEventHandler.InboxEvent): String {
    val event = inboxEventHelper.toDomainEvent(inboxEvent)
    return event.getAdditionalInformation("fromUsername")
  }

  @Transactional
  override fun handle(inboxEvent: InboxEventHandler.InboxEvent): InboxEventHandler.Result {
    val event = inboxEventHelper.toDomainEvent(inboxEvent)
    val fromUsername = event.getAdditionalInformation("fromUsername").uppercase()

    return when (
      val existingUser =
        userRepository.findByUsernameAndAuthSource(Username(fromUsername), AuthSource.DELIUS)
    ) {
      null -> {
        log.info("No delius user found with name [$fromUsername]. Ignoring.")
        InboxEventHandler.Result.IGNORED
      }

      else -> {
        val toUsername = event.getAdditionalInformation("toUsername").uppercase()
        existingUser.username = toUsername
        log.info("Updating delius username [$fromUsername] to [$toUsername]")
        InboxEventHandler.Result.PROCESSED
      }
    }
  }
}
