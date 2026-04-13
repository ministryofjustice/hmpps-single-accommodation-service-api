package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3BookingStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityKeys
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.util.minusWorkingHours
import java.time.Clock
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit.DAYS

private const val MAX_DAYS_BEFORE_RELEASE = 28
private const val MIN_WORKING_HOURS_BEFORE_RELEASE = 12

private val acceptableCas3Statuses = listOf(
  Cas3BookingStatus.DEPARTED,
  Cas3BookingStatus.NOT_ARRIVED,
  Cas3BookingStatus.CANCELLED,
  Cas3BookingStatus.CLOSED,
)

fun buildCas3Action(data: DomainData, clock: Clock) = when (data.cas3Application?.applicationStatus) {
  Cas3ApplicationStatus.PLACED -> {
    if (!acceptableCas3Statuses.contains(data.cas3Application.bookingStatus)) {
      error("Invalid booking status: ${data.cas3Application.bookingStatus}")
    }
    EligibilityKeys.CREATE_PLACEMENT
  }

  Cas3ApplicationStatus.AWAITING_PLACEMENT,
  Cas3ApplicationStatus.PENDING,
  -> EligibilityKeys.CREATE_PLACEMENT

  Cas3ApplicationStatus.REQUESTED_FURTHER_INFORMATION,
  -> EligibilityKeys.PROVIDE_INFORMATION

  Cas3ApplicationStatus.IN_PROGRESS,
  Cas3ApplicationStatus.SUBMITTED,
  Cas3ApplicationStatus.REJECTED,
  Cas3ApplicationStatus.INAPPLICABLE,
  Cas3ApplicationStatus.WITHDRAWN,
  null,
  -> buildStartCas3ReferralAction(data, clock)
}

private fun buildStartCas3ReferralAction(data: DomainData, clock: Clock): String {
  val releaseDate = data.releaseDate
    ?: error("Release date for crn: ${data.crn} is null")

  if (isWithinSubmissionWindow(releaseDate, clock)) return EligibilityKeys.START_CAS3_REFERRAL
  return buildReferralStartDateAction(releaseDate, clock)
}

// Deadline is 12 working hours before release (assumes 9am release, 9am-5pm working hours)
private fun isWithinSubmissionWindow(releaseDate: LocalDate, clock: Clock): Boolean {
  val now = ZonedDateTime.now(clock)
  val releaseDateTime = releaseDate
    .atTime(9, 0)
    .atZone(clock.zone)

  val calendarDaysUntilRelease = DAYS.between(now.toLocalDate(), releaseDate).toInt()
  val submissionDeadline = releaseDateTime.minusWorkingHours(MIN_WORKING_HOURS_BEFORE_RELEASE)

  return calendarDaysUntilRelease <= MAX_DAYS_BEFORE_RELEASE && now.isBefore(submissionDeadline)
}

private fun buildReferralStartDateAction(releaseDate: LocalDate, clock: Clock): String {
  val daysUntilWindowOpens = DAYS.between(LocalDate.now(clock), releaseDate).toInt() - MAX_DAYS_BEFORE_RELEASE

  return when {
    daysUntilWindowOpens > 1 -> "${EligibilityKeys.START_CAS3_REFERRAL} in $daysUntilWindowOpens days"
    else -> "${EligibilityKeys.START_CAS3_REFERRAL} in 1 day"
  }
}
