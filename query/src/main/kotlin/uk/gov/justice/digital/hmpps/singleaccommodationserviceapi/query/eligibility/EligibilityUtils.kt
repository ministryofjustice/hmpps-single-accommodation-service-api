package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility

import java.time.Clock
import java.time.LocalDate

const val DTR_EXPIRY_WEEKS = 26L

fun isDtrExpired(submissionDate: LocalDate?, clock: Clock): Boolean = !isLessThanXWeeksInThePast(submissionDate, LocalDate.now(clock), DTR_EXPIRY_WEEKS)

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
