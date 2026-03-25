package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationdelius

import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDate
import java.time.ZonedDateTime

data class CaseSummaries(val cases: List<CaseSummary>)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class CaseSummary(
  val crn: String,
  val nomsId: String?,
  val pnc: String?,
  val name: CaseSummaryName,
  val dateOfBirth: LocalDate,
  val gender: String?,
  val profile: Profile?,
  val manager: Manager,
  val currentExclusion: Boolean,
  val currentRestriction: Boolean,
)

data class CaseSummaryName(val forename: String, val surname: String, val middleNames: List<String>)

data class Profile(
  val ethnicity: String?,
  val genderIdentity: String?,
  val nationality: String?,
  val religion: String?,
)

data class Manager(val team: Team)

data class MappaDetail(
  val level: Int?,
  val levelDescription: String?,
  val category: Int?,
  val categoryDescription: String?,
  val startDate: LocalDate,
  val lastUpdated: ZonedDateTime,
)

data class Offence(
  val id: String,
  val code: String,
  val description: String,
  val mainCategoryDescription: String,
  val subCategoryDescription: String,
  val date: LocalDate?,
  val main: Boolean,
  val eventId: Long,
  val eventNumber: String,
)

data class Registration(val code: String, val description: String, val startDate: LocalDate)
