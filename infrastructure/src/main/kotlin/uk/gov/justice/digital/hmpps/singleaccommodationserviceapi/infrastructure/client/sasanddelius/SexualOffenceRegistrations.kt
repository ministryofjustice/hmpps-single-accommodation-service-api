package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.sasanddelius

import java.time.LocalDate

data class SexualOffenceRegistrations(
  val crn: String,
  val sexualOffenceRegistrations: List<SexualOffenceRegistration>?,
)

data class SexualOffenceRegistration(
  val type: OffenceType,
  val category: CategoryType?,
  val startDate: LocalDate,
  val nextReviewDate: LocalDate?,
  val endDate: LocalDate?,
)

data class OffenceType(
  val code: SexualOffenceRegistrationCode,
  val description: String,
)

data class CategoryType(
  val code: String,
  val description: String,
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
