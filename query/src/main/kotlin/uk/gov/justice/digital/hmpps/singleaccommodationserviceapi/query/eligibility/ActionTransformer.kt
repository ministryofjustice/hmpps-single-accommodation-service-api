package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.RuleAction
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.enums.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.enums.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import java.time.LocalDate
import java.time.temporal.ChronoUnit.DAYS
import java.time.Clock
import java.time.temporal.ChronoUnit.YEARS


fun buildAction(data: DomainData, clock: Clock) = when (data.cas1Application?.applicationStatus) {
  Cas1ApplicationStatus.PLACEMENT_ALLOCATED
    -> when (data.cas1Application.placementStatus) {

    Cas1PlacementStatus.UPCOMING,
    Cas1PlacementStatus.ARRIVED
      -> error("No action needed")

    Cas1PlacementStatus.DEPARTED,
    Cas1PlacementStatus.NOT_ARRIVED,
    Cas1PlacementStatus.CANCELLED
      -> RuleAction("Create Placement")

    null -> error("Null Placement Status for ${data.cas1Application.applicationStatus} ${data.cas1Application.id}")
  }

  Cas1ApplicationStatus.AWAITING_PLACEMENT,
  Cas1ApplicationStatus.PENDING_PLACEMENT_REQUEST
    -> RuleAction("Create Placement")

  Cas1ApplicationStatus.AWAITING_ASSESSMENT,
  Cas1ApplicationStatus.UNALLOCATED_ASSESSMENT,
  Cas1ApplicationStatus.ASSESSMENT_IN_PROGRESS,
    -> RuleAction("Await Assessment", true)

  Cas1ApplicationStatus.REQUEST_FOR_FURTHER_INFORMATION
    -> RuleAction("Provide Information")

  else -> {
    if (data.releaseDate == null) {
       error("Release date for crn: ${data.crn} is null")
    }
    val yearsUntilRelease = YEARS.between(
      LocalDate.now(clock),
      data.releaseDate,
    )
    val isWithin1Year = yearsUntilRelease < 1
    val actionText = "Start approved premise referral"

     if (isWithin1Year) {
      RuleAction(actionText)
    } else {
      val dateToStartReferral = data.releaseDate.minusYears(1)
      val daysUntilReferralMustStart = DAYS.between(LocalDate.now(clock), dateToStartReferral).toInt()
      if (daysUntilReferralMustStart > 1) {
        RuleAction("$actionText in $daysUntilReferralMustStart days", true)
      } else if (daysUntilReferralMustStart < 1) {
        RuleAction(actionText)
      } else {
        RuleAction("$actionText in 1 day", true)
      }
    }
  }
}