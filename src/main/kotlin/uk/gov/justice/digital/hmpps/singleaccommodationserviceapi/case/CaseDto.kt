package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.case

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.probationintegrationoasys.RiskLevel
import java.time.LocalDate

data class CaseDto(
  val name: String,
  val dateOfBirth: LocalDate?,
  val crn: String?,
  val prisonNumber: String?,
  val tier: String?,
  val riskLevel: RiskLevel?,
  val pncReference: String?,
  val assignedTo: AssignedToDto?,
  val currentAccommodation: CurrentAccommodationDto?,
  val nextAccommodation: NextAccommodationDto?,
)

data class AssignedToDto(val id: Long, val name: String)
data class CurrentAccommodationDto(val type: String, val endDate: LocalDate)
data class NextAccommodationDto(val type: String, val startDate: LocalDate)
