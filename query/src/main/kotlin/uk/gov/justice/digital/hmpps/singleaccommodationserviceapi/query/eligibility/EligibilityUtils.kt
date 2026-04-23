package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility

import java.time.LocalDate
import java.time.temporal.ChronoUnit.DAYS

fun buildUpcomingAction(endDate: LocalDate, today: LocalDate, initialText: String): String {
  val dateToStartReferral = endDate.minusYears(1)
  val daysUntilReferralMustStart = DAYS.between(today, dateToStartReferral).toInt()
  val formattedMonth = dateToStartReferral.month.name.lowercase()
    .replaceFirstChar { it.uppercase() }

  val formattedDate = "(${dateToStartReferral.dayOfMonth} $formattedMonth ${dateToStartReferral.year})"

  return when {
    daysUntilReferralMustStart > 1
    -> "$initialText in $daysUntilReferralMustStart days $formattedDate"

    daysUntilReferralMustStart < 1 -> initialText

    else -> "$initialText in 1 day $formattedDate"
  }
}

fun isWithin56Days(endDate: LocalDate?, today: LocalDate): Boolean {
  if (endDate == null) return true
  val fiftySixDaysFromNow = today.plusDays(56)
  return endDate <= fiftySixDaysFromNow
}

fun isWithinOneYear(endDate: LocalDate?, today: LocalDate): Boolean {
  if (endDate == null) return false
  val oneYearFromNow = today.plusYears(1)
  return endDate <= oneYearFromNow
}
