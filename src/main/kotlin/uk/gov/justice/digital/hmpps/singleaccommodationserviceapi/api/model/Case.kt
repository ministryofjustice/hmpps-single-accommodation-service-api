package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.model

data class Name(
  val forename: String,
  val surname: String,
  val middleNames: List<String>,
)

data class Profile(
  val ethnicity: String?,
  val genderIdentity: String?,
  val nationality: String?,
  val religion: String?,
)

data class Ldu(
  val code: String?,
  val name: String?,
)

data class Borough(
  val code: String?,
  val description: String?,
)

data class Team(
  val code: String?,
  val name: String?,
  val ldu: Ldu?,
  val borough: Borough?,
  val startDate: String?,
  val endDate: String?,
)

data class Manager(
  val team: Team?,
)

data class Case(
  val crn: String,
  val nomsId: String?,
  val pnc: String?,
  val name: Name,
  val dateOfBirth: String?,
  val gender: String?,
  val profile: Profile?,
  val manager: Manager?,
  val currentExclusion: Boolean?,
  val currentRestriction: Boolean?,
)
