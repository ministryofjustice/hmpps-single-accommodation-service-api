package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.probationintegrationdelius.case

import java.time.LocalDate

data class Case(
  val name: String,
  val dateOfBirth: LocalDate?,
  val crn: String?,
  val prisonNumber: String?,
  val tier: String?,
  val rosh: String?,
  val pncReference: String?,
  val assignedTo: AssignedTo?,
  val currentAccommodation: CurrentAccommodation?,
  val nextAccommodation: NextAccommodation?,
)

data class AssignedTo(val id: Long, val name: String)
data class CurrentAccommodation(val type: String, val endDate: LocalDate)
data class NextAccommodation(val type: String, val startDate: LocalDate)
