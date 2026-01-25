package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.SingleAccommodationServiceDomainEventType
import java.util.UUID

@Configuration
class HmppsDomainEventUrlConfig(
  @Value($$"${event.details-url.proposed-accommodation-created}")
  val proposedAccommodationCreatedEventDetailsUrl: String,
) {

  fun getUrlForDomainEventId(domainEventType: SingleAccommodationServiceDomainEventType, id: UUID): String {
    val template = when (domainEventType) {
      SingleAccommodationServiceDomainEventType.PROPOSED_ACCOMMODATION_CREATED -> UrlTemplate(proposedAccommodationCreatedEventDetailsUrl)
    }
    return template.resolve("id", id.toString())
  }
}

class UrlTemplate(val template: String) {
  fun resolve(args: Map<String, String>) = args.entries.fold(template) { acc, (key, value) -> acc.replace("#$key", value) }
  fun resolve(paramName: String, paramValue: String) = resolve(mapOf(paramName to paramValue))
}
