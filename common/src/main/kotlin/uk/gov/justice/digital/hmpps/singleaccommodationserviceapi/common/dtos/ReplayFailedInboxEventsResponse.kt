package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos

data class ReplayFailedInboxEventsResponse(
  val replayAll: Boolean,
  val replayedCount: Int,
)
