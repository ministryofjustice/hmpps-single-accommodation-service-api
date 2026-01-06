package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.RuleAction
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.util.minusWorkingHours
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.enums.Cas3ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.enums.Cas3PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.ActionKeys.CREATE_PLACEMENT
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.ActionKeys.PROVIDE_INFORMATION
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.ActionKeys.START_CAS3_REFERRAL
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit.DAYS

private const val MAX_DAYS_BEFORE_RELEASE = 28
private const val MIN_WORKING_HOURS_BEFORE_RELEASE = 12

fun buildCas3Action(data: DomainData, clock: Clock) = when (data.cas3Application?.applicationStatus) {
  Cas3ApplicationStatus.PLACED -> {
    val acceptableStatuses = listOf(
      Cas3PlacementStatus.DEPARTED,
      Cas3PlacementStatus.NOT_ARRIVED,
      Cas3PlacementStatus.CANCELLED,
      Cas3PlacementStatus.CLOSED,
    )
    if (!acceptableStatuses.contains(data.cas3Application.placementStatus)) {
      error("Invalid placement status: ${data.cas3Application.placementStatus}")
    }
    RuleAction(CREATE_PLACEMENT)
  }

  Cas3ApplicationStatus.AWAITING_PLACEMENT,
  Cas3ApplicationStatus.PENDING,
    -> RuleAction(CREATE_PLACEMENT)

  Cas3ApplicationStatus.REQUESTED_FURTHER_INFORMATION
    -> RuleAction(PROVIDE_INFORMATION)

  Cas3ApplicationStatus.IN_PROGRESS,
  Cas3ApplicationStatus.SUBMITTED,
  Cas3ApplicationStatus.REJECTED,
  Cas3ApplicationStatus.INAPPLICABLE,
  Cas3ApplicationStatus.WITHDRAWN,
  null -> buildStartCas3ReferralAction(data, clock)
}

private fun buildStartCas3ReferralAction(data: DomainData, clock: Clock): RuleAction {
  val releaseDate = data.releaseDate
    ?: error("Release date for crn: ${data.crn} is null")

  if (isWithinSubmissionWindow(releaseDate, clock)) return RuleAction(START_CAS3_REFERRAL)
  return buildReferralStartDateAction(releaseDate, clock)
}

// Deadline is 12 working hours before release (assumes 9am release, 9am-5pm working hours)
private fun isWithinSubmissionWindow(releaseDate: LocalDate, clock: Clock): Boolean {
  val zone = clock.zone
  val now = clock.instant().atZone(zone)
  val today = now.toLocalDate()

  val calendarDaysUntilRelease = DAYS.between(today, releaseDate).toInt()

  val releaseTime = releaseDate.atTime(LocalTime.of(9, 0)).atZone(zone)
  val submissionDeadline = releaseTime.minusWorkingHours(MIN_WORKING_HOURS_BEFORE_RELEASE)

  return calendarDaysUntilRelease <= MAX_DAYS_BEFORE_RELEASE && now.isBefore(submissionDeadline)
}

private fun buildReferralStartDateAction(releaseDate: LocalDate, clock: Clock): RuleAction {
  val daysUntilWindowOpens = DAYS.between(LocalDate.now(clock), releaseDate).toInt() - MAX_DAYS_BEFORE_RELEASE

  return when {
    daysUntilWindowOpens > 1 -> RuleAction("$START_CAS3_REFERRAL in $daysUntilWindowOpens days", true)
    else -> RuleAction("$START_CAS3_REFERRAL in 1 day", true)
  }
}
