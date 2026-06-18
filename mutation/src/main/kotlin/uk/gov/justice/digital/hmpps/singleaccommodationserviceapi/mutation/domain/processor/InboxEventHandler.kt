package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.processor

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.IncomingHmppsDomainEventType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.InboxEventEntity

/**
 * Handles processing of a specific inbox event type. Each handler is responsible for a single event
 * type and manages its own transaction boundary. Add new handlers by implementing this interface
 * and registering as a Spring bean.
 *
 * Events with the same [getPartitionKey] are processed sequentially to avoid concurrent updates to
 * the same resource (e.g. case per CRN). Events with different keys are processed in parallel.
 */
interface InboxEventHandler {

  /** The event type this handler supports. Only one handler per type*/
  fun supportedEventType(): IncomingHmppsDomainEventType

  /**
   * Partition key for serialising processing. Events with the same key are never processed
   * concurrently. Return null to process independently (each event in its own partition).
   */
  fun getPartitionKey(inboxEvent: InboxEventEntity): String? = null

  /**
   * Process the inbox event. Should run in its own transaction to make success or failure isolated per
   * event.
   *
   * If an exception occurs do not rethrow, instead return [Result.FAILED], allowing the dispatcher to continue with
   * the next event
   *
   */
  fun handle(inboxEvent: InboxEventEntity): Result

  enum class Result {
    PROCESSED,
    NOT_PROCESSED,
    FAILED,
  }
}
