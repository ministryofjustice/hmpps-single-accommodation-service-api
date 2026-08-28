package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event

enum class IncomingHmppsDomainEventType(
  val typeName: String,
) {
  CPR_PROBATION_ADDRESS_CREATED("core-person-record.probation.address.created"),
  CPR_PROBATION_ADDRESS_UPDATED("core-person-record.probation.address.updated"),
  CPR_PROBATION_ADDRESS_DELETED("core-person-record.probation.address.deleted"),
  CPR_PROBATION_RECORD_UPDATED("core-person-record.probation.record.updated"),
  TIER_CALCULATION_CHANGED("tier.calculation.changed"),
  PERSON_COMMUNITY_MANAGER_ALLOCATED("person.community.manager.allocated"),
  PRISONER_OFFENDER_SEARCH_PRISONER_UPDATED("prisoner-offender-search.prisoner.updated"),
  PRISONER_OFFENDER_SEARCH_PRISONER_RECEIVED("prisoner-offender-search.prisoner.received"),
  PRISONER_OFFENDER_SEARCH_PRISONER_RELEASED("prisoner-offender-search.prisoner.released"),
  APPROVED_PREMISES_BOOKING_CANCELLED("approved-premises.booking.cancelled"),
  APPROVED_PREMISES_BOOKING_CHANGED("approved-premises.booking.changed"),
  APPROVED_PREMISES_BOOKING_NOT_ARRIVED("approved-premises.booking.not-arrived"),
  APPROVED_PREMISES_BOOKING_MADE("approved-premises.booking.made"),
  ;

  companion object {
    fun forEventType(eventType: String): IncomingHmppsDomainEventType? = entries.find { it.typeName == eventType }
  }
}
