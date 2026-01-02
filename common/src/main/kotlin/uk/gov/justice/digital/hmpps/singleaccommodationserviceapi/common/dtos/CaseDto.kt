package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos

import java.time.LocalDate

data class CaseDto(
  val name: String,
  val dateOfBirth: LocalDate?,
  val crn: String?,
  val prisonNumber: String?,
  val photoUrl: String?,
  val tier: TierScore?,
  val riskLevel: RiskLevel?,
  val pncReference: String?,
  val assignedTo: AssignedToDto?,
  val currentAccommodation: AccommodationDetail?,
  val nextAccommodation: AccommodationDetail?,
)

data class AssignedToDto(val id: Long, val name: String)

enum class RiskLevel(val priority: Int) {
  LOW(1),
  MEDIUM(2),
  HIGH(3),
  VERY_HIGH(4),
}

enum class TierScore {
  A3,
  A2,
  A1,
  B3,
  B2,
  B1,
  C3,
  C2,
  C1,
  D3,
  D2,
  D1,
  A3S,
  A2S,
  A1S,
  B3S,
  B2S,
  B1S,
  C3S,
  C2S,
  C1S,
  D3S,
  D2S,
  D1S;
}