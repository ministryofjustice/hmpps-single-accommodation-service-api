package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos

import java.time.LocalDate

enum class UserAccess { LIMITED, FULL, UNKNOWN }

data class CaseDto(
  val forename: String? = null,
  val middleNames: String? = null,
  val surname: String? = null,
  val dateOfBirth: LocalDate? = null,
  val crn: String,
  val prisonNumber: String? = null,
  val photoUrl: String? = null,
  val tierScore: String? = null,
  val riskLevel: RiskLevel? = null,
  val pncReference: String? = null,
  val assignedTo: AssignedToDto? = null,
  val userAccess: UserAccess,
  val limitedAccess: Boolean?,
  val accommodationSummaries: AccommodationSummariesDto? = null,
  val sexualOffenceRegistrations: List<SexualOffenceRegistrationCode> = emptyList(),
  val teamCode: String? = null,
)

enum class SexualOffenceRegistrationCode(val code: String, val description: String) {
  RSC("RSC", "(SC1 & SC2)"),
  ANSO("ANSO", "Non Registered Sex Offender"),
  ARSO("ARSO", "Registered Sex Offender"),
  RCHD("RCHD", "Risk to Children"),
  SHPO("SHPO", "Sexual Harm Prevention Order / Sexual Risk Order"),
  CSEP("CSEP", "Child Sexual Exploitation – Perpetrator"),
  ALT13("ALT13", "ALT Child Sexual Exploitation history"),
  ALT3("ALT3", "ALT Registered Sex Offender"),
  SOPS("SOPS", "Sex Offences Prevention Order"),
}

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

enum class PeopleType {
  HOUSED,
  NFA_RISK,
}
