package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.sasanddelius

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.PageMetadata
import java.time.LocalDate

data class CaseList(
  val cases: List<Case>,
  val page: PageMetadata,
)

data class Case(
  val crn: String,
  val name: Name,
  val nomsNumber: String?,
  val pncNumber: String?,
  val dateOfBirth: LocalDate,
  val staff: Officer,
  val team: Team,
  val gender: String,
  val roshLevel: RoshLevel?,
  val userExcluded: Boolean,
  val userRestricted: Boolean,
  val exclusionMessage: String?,
  val restrictionMessage: String?,
  val limitedAccess: Boolean,
)

data class Name(
  val forename: String,
  val middleName: String?,
  val surname: String,
) {
  val fullName: String
    get() = listOfNotNull(
      forename,
      middleName?.takeIf { it.isNotBlank() },
      surname,
    ).joinToString(" ")
}

data class Officer(
  val name: Name,
  val username: String,
  val code: String,
)

data class RoshLevel(
  val code: String,
  val description: String,
)

data class Team(
  val code: String,
  val description: String,
)
