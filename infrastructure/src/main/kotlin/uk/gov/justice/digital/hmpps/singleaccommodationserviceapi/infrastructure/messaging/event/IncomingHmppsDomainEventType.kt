package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event

enum class IncomingHmppsDomainEventType(
  val typeName: String,
  val typeDescription: String,
) {
  TIER_CALCULATION_CHANGED(
    "tier.calculation.changed",
    "Tier calculation resulted in an updated tier value",
  ),
  CASE_ALLOCATED(
    "person.community.manager.allocated",
    "Case Allocated to Probation Practitioner",
  ),
  ;

  companion object {
    fun forEventType(eventType: String): IncomingHmppsDomainEventType? = entries.find { it.typeName == eventType }
  }
}
