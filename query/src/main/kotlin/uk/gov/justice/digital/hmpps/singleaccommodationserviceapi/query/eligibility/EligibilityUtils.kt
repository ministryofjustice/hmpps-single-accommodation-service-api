package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseActionType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
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

fun mapSexToCaseActionType(sexCode: SexCode?): CaseActionType = if (sexCode == SexCode.M) {
  CaseActionType.SUBMIT_CRS_ACCOMMODATION_REFERRAL
} else {
  CaseActionType.SUBMIT_CRS_REFERRAL
}
