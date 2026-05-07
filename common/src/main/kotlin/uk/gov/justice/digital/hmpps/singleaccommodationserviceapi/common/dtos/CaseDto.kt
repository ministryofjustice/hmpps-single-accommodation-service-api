package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos

import java.time.LocalDate

enum class LAOStatus { RESTRICTED, EXCLUDED, NONE, UNKNOWN }

data class CaseDto(
  val name: String? = null,
  val dateOfBirth: LocalDate? = null,
  val crn: String,
  val prisonNumber: String? = null,
  val photoUrl: String? = null,
  val tierScore: TierScore? = null,
  val riskLevel: RiskLevel? = null,
  val pncReference: String? = null,
  val assignedTo: AssignedToDto? = null,
  val currentAccommodation: AccommodationDetail? = null,
  val nextAccommodation: AccommodationDetail? = null,
  val status: Status? = null,
  val actions: List<String> = emptyList(),
  val laoStatus: LAOStatus,
)

data class AssignedToDto(
  val forename: String,
  val surname: String,
  val username: String? = null,
  val staffCode: String? = null,
)

enum class RiskLevel(val code: String) {
  LOW("RLRH"),
  MEDIUM("RMRH"),
  HIGH("RHRH"),
  VERY_HIGH("RVHR"),
  ;

  companion object {
    fun findByCode(code: String) = RiskLevel.entries.firstOrNull { it.code == code }
  }
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
  D0,
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
  D1S,
}

enum class Status {
  RISK_OF_NO_FIXED_ABODE,
  NO_FIXED_ABODE,
  TRANSIENT,
  SETTLED,
}
