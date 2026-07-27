package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos

import java.time.LocalDate

enum class UserAccess { LIMITED, FULL, UNKNOWN }

data class CaseDto(
  @Deprecated("Use personNames instead")
  val name: String? = null,
  val personNames: PersonNamesDto? = null,
  val dateOfBirth: LocalDate? = null,
  val crn: String,
  val prisonNumber: String? = null,
  val photoUrl: String? = null,
  val tierScore: String? = null,
  val riskLevel: RiskLevel? = null,
  val pncReference: String? = null,
  val assignedTo: AssignedToDto? = null,
  val actions: List<CaseAction> = emptyList(),
  val userAccess: UserAccess,
  val limitedAccess: Boolean?,
)

data class PersonNamesDto(
  val forename: String,
  val middleNames: String? = null,
  val surname: String,
)

data class AssignedToDto(
  val forename: String,
  val surname: String,
  val username: String? = null,
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
