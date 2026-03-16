package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationdelius

import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDate

data class CaseSummaries(val cases: List<CaseSummary>)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class CaseSummary(
  val crn: String,
  val nomsId: String?,
  val pnc: String?,
  val name: Name,
  val dateOfBirth: LocalDate,
  val gender: String?,
  val profile: Profile?,
  val manager: Manager,
  val currentExclusion: Boolean,
  val currentRestriction: Boolean,
)

data class Name(val forename: String, val surname: String, val middleNames: List<String>)

data class Profile(
  val ethnicity: String?,
  val genderIdentity: String?,
  val nationality: String?,
  val religion: String?,
)

data class Manager(val team: Team)
