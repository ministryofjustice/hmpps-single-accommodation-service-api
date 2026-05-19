package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility

import java.time.LocalDate
import java.time.temporal.ChronoUnit.DAYS

fun buildUpcomingAction(today: LocalDate, initialText: String, dateToStartReferral: LocalDate): String {
  val daysUntilReferralMustStart = DAYS.between(today, dateToStartReferral).toInt()

  return when {
    daysUntilReferralMustStart > 1 -> "$initialText in $daysUntilReferralMustStart days"
    daysUntilReferralMustStart == 1 -> "$initialText in 1 day"
    else -> initialText
  }
}

fun isLessThanXWeeksInTheFuture(endDate: LocalDate?, today: LocalDate, numOfWeeks: Long): Boolean {
  if (endDate == null) return true
  val xWeeksFromNow = today.plusWeeks(numOfWeeks)
  return endDate <= xWeeksFromNow
}

fun isLessThanOneYearInTheFuture(endDate: LocalDate?, today: LocalDate): Boolean {
  if (endDate == null) return true
  val oneYearFromNow = today.plusYears(1)
  return endDate <= oneYearFromNow
}

fun isLessThanXWeeksInThePast(endDate: LocalDate?, today: LocalDate, numOfWeeks: Long): Boolean {
  if (endDate == null) return false
  val xWeeksInThePast = today.minusWeeks(numOfWeeks)

  return endDate >= xWeeksInThePast
}
