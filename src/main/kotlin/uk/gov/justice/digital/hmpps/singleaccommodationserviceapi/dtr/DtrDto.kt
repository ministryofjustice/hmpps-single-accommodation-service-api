package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.dtr

import java.time.LocalDateTime
import java.util.UUID

data class DtrDto(
  val id: UUID,
  val crn: String,
  val submittedTo: String?,
  val reference: String?,
  val submitted: LocalDateTime?,
  val status: String?,
  val outcome: String?,
)
