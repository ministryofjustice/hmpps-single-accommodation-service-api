package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.IncomingHmppsDomainEventType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.InboxEventEntity

/**
 * Handles processing of a specific inbox event type. Each handler is responsible for a single event
 * type and manages its own transaction boundary. Add new handlers by implementing this interface
 * and registering as a Spring bean.
 */
interface InboxEventHandler {

  /** The event type this handler supports. Only one handler per type*/
  fun supportedEventType(): IncomingHmppsDomainEventType

  /**
   * Process the inbox event. Should run in its own transaction to make success or failure isolated per
   * event. Handler must update inboxEvent.processedStatus and inboxEvent.processedAt before
   * returning.
   *
   * Do not rethrow - persist FAILED status and allow dispatcher to continue with next event
   *
   */
  fun handle(inboxEvent: InboxEventEntity)
}