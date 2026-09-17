package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos

import java.util.UUID

data class ReplayFailedInboxEventsResponse(
  val replayAll: Boolean,
  val replayedCount: Int,
  val replayedMessageIds: Set<UUID>,
)
