package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseAccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.PeopleType
import java.time.LocalDate

fun List<CaseDto>.filtered(peopleType: PeopleType?) = when (peopleType) {
  null -> this

  PeopleType.NFA_RISK ->
    this.filter { it.accommodationSummaries?.caseAccommodationStatus != CaseAccommodationStatus.SETTLED }

  PeopleType.HOUSED ->
    this.filter { it.accommodationSummaries?.caseAccommodationStatus == CaseAccommodationStatus.SETTLED }
}

fun List<CaseDto>.sortCases(today: LocalDate): List<CaseDto> = sortedWith(
  compareBy<CaseDto, Int?>(nullsFirst()) { it.accommodationSummaries?.caseAccommodationStatus?.sortOrder }
    .thenBy { it.sortByStatusDate(today) }
    .thenBy { it.accommodationSummaries?.caseAccommodationStatusDate }
    .thenBy { it.surname?.lowercase() }
    .thenBy { it.forename?.lowercase() }
    .thenBy { it.crn },
)

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
