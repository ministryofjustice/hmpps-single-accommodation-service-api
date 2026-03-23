package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationsasdelius

import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDate

data class CaseList(val cases: List<CaseListItem>)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class CaseListItem(
  val crn: String,
  val name: Name,
  val pncNumber: String?, // TODO not currently included in endpoint
  val dateOfBirth: LocalDate,
  val staff: Staff,
  val team: Team,
  val gender: String,
  val userExcluded: Boolean,
  val userRestricted: Boolean,
  val roshLevel: RoshLevel?, // TODO not currently included in endpoint
  val expectedReleaseDate: LocalDate?, // TODO not currently included in endpoint
  val nomsNumber: String?, // TODO not currently included in endpoint
)

data class RoshLevel(
  val description: String,
  val code: String,
)

data class Staff(
  val name: StaffName,
  val username: String,
  val code: String,
)

data class Team(
  val description: String,
  val code: String,
)

data class Name(val forename: String, val surname: String, val middleName: String)

data class StaffName(val forename: String, val surname: String)
