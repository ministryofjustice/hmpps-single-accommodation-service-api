package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.enums

enum class Cas1RequestForPlacementStatus(val value: String) {
  requestUnsubmitted("request_unsubmitted"),
  requestRejected("request_rejected"),
  requestSubmitted("request_submitted"),
  awaitingMatch("awaiting_match"),
  requestWithdrawn("request_withdrawn"),
  placementBooked("placement_booked"),
}