package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event

enum class IncomingHmppsDomainEventType(
  val typeName: String,
  val typeDescription: String,
) {
  TIER_CALCULATION_COMPLETE(
    "tier.calculation.complete",
    "Tier calculation complete from Tier service",
  ),
  CASE_ALLOCATED_TO_PROBATION_PRACTITIONER(
    "person.community.manager.allocated",
    "Case Allocated to Probation Practitioner",
  ),
  ;

  companion object {
    fun from(eventType: String): IncomingHmppsDomainEventType? = entries.find { it.typeName == eventType }
  }
}
