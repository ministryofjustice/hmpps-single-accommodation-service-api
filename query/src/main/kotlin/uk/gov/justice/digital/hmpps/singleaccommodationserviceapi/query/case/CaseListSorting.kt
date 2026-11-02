package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseAccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.PeopleType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.RiskLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.UserAccess
import java.time.LocalDate

fun List<CaseDto>.filterCases(
  searchTerm: String?,
  riskLevel: RiskLevel?,
  teamCode: String?,
  username: String?,
  peopleType: PeopleType?,
) = this
  .filter {
    // this matches on either team or name. If team is provided, when return the cases on the team, otherwise
    // we return only the cases that match with the username, if proved. Matches on all when no team or username is
    // provided (required for users with a custom case list)
    if (!teamCode.isNullOrBlank()) {
      it.matchesTeam(teamCode)
    } else {
      it.matchesUser(username)
    } &&
      it.matchesSearch(searchTerm) &&
      it.matchesRiskLevel(riskLevel) &&
      it.matchesPeopleType(peopleType)
  }
  .obfuscateLimitedAccessCases()

private fun CaseDto.matchesPeopleType(peopleType: PeopleType?) = when (peopleType) {
  null -> true

  PeopleType.NFA_RISK ->
    this.accommodationSummaries?.caseAccommodationStatus != CaseAccommodationStatus.SETTLED

  PeopleType.HOUSED ->
    this.accommodationSummaries?.caseAccommodationStatus == CaseAccommodationStatus.SETTLED
}

private fun CaseDto.matchesUser(username: String?) = username == null || username.equals(this.assignedTo?.username, ignoreCase = true)

private fun CaseDto.matchesTeam(teamCode: String): Boolean = when {
  teamCode.equals(this.teamCode, ignoreCase = true) -> true
  else -> false
}

private fun CaseDto.matchesRiskLevel(riskLevel: RiskLevel?): Boolean = when {
  riskLevel == null -> true
  userAccess == UserAccess.FULL && this.riskLevel == riskLevel -> true
  else -> false
}

private fun CaseDto.matchesSearch(searchTerm: String?): Boolean = when {
  searchTerm.isNullOrBlank() -> true

  crn.trim().equals(searchTerm, true) -> true

  prisonNumber?.trim().equals(searchTerm, true) -> true

  userAccess == UserAccess.FULL &&
    searchTerm
      .split(" ")
      .filter { it.isNotBlank() }
      .all {
        listOfNotNull(forename, surname)
          .joinToString(" ")
          .contains(it, ignoreCase = true)
      } -> true

  else -> false
}

fun List<CaseDto>.sortCases(today: LocalDate): List<CaseDto> = sortedWith(
  compareBy<CaseDto, Int?>(nullsFirst()) { it.accommodationSummaries?.caseAccommodationStatus?.sortOrder }
    .thenBy { it.sortByStatusDate(today) }
    .thenBy { it.accommodationSummaries?.caseAccommodationStatusDate }
    .thenBy { it.surname?.lowercase() }
    .thenBy { it.forename?.lowercase() }
    .thenBy { it.crn },
)

/*
this has been added during a refactor to retain existing results. It's probably not needed.
 */
fun List<CaseDto>.obfuscateLimitedAccessCases(): List<CaseDto> = map { it.obfuscateIfLimitedAccess() }

fun CaseDto.obfuscateIfLimitedAccess(): CaseDto = if (userAccess != UserAccess.FULL) copy(assignedTo = null, teamCode = null) else this

private fun CaseDto.sortByStatusDate(today: LocalDate): Comparable<*>? {
  val statusDate = accommodationSummaries?.caseAccommodationStatusDate
  return when (accommodationSummaries?.caseAccommodationStatus) {
    CaseAccommodationStatus.TRANSIENT,
    CaseAccommodationStatus.SETTLED,
    -> when {
      statusDate?.isAfter(today) == true -> 1
      statusDate != null -> 2
      else -> 0
    }
    else -> statusDate
  }
}
