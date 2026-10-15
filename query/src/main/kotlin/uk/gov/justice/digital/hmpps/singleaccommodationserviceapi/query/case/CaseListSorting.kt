package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseAccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseDto
import java.time.Clock
import java.time.LocalDate
import kotlin.comparisons.nullsFirst

fun List<CaseDto>.sortCases(clock: Clock): List<CaseDto> {
  val today = LocalDate.now(clock)
  return sortedWith(
    compareBy<CaseDto, Int?>(nullsFirst()) { it.accommodationSummaries?.caseAccommodationStatus?.sortOrder }
      .thenBy { it.sortByStatusDate(today) }
      .thenBy { it.accommodationSummaries?.caseAccommodationStatusDate }
      .thenBy { it.surname?.lowercase() }
      .thenBy { it.forename?.lowercase() }
      .thenBy { it.crn },
  )
}

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
