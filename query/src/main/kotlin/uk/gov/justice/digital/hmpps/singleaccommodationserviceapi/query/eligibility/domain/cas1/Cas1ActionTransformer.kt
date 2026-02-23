package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.RuleAction
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.enums.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.enums.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.ActionKeys.AWAIT_ASSESSMENT
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.ActionKeys.CREATE_PLACEMENT
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.ActionKeys.PROVIDE_INFORMATION
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.ActionKeys.START_APPROVED_PREMISE_APPLICATION
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import java.time.Clock
import java.time.LocalDate
import java.time.temporal.ChronoUnit.DAYS

object Cas1ActionTransformer {
  fun buildCas1Action(data: DomainData, clock: Clock, isWithinOneYear: Boolean) = when (data.cas1Application?.applicationStatus) {
    Cas1ApplicationStatus.PLACEMENT_ALLOCATED,
      -> {
      val acceptableStatus = listOf(
        Cas1PlacementStatus.DEPARTED,
        Cas1PlacementStatus.NOT_ARRIVED,
        Cas1PlacementStatus.CANCELLED,
      )
      if (!acceptableStatus.contains(data.cas1Application.placementStatus)) error("Invalid placement status: ${data.cas1Application.placementStatus}")
      RuleAction(CREATE_PLACEMENT)
    }

    Cas1ApplicationStatus.AWAITING_PLACEMENT,
    Cas1ApplicationStatus.PENDING_PLACEMENT_REQUEST,
      -> RuleAction(CREATE_PLACEMENT)

    Cas1ApplicationStatus.AWAITING_ASSESSMENT,
    Cas1ApplicationStatus.UNALLOCATED_ASSESSMENT,
    Cas1ApplicationStatus.ASSESSMENT_IN_PROGRESS,
      -> RuleAction(AWAIT_ASSESSMENT, true)

    Cas1ApplicationStatus.REQUEST_FOR_FURTHER_INFORMATION,
      -> RuleAction(PROVIDE_INFORMATION)

    Cas1ApplicationStatus.STARTED,
    Cas1ApplicationStatus.REJECTED,
    Cas1ApplicationStatus.INAPPLICABLE,
    Cas1ApplicationStatus.WITHDRAWN,
    Cas1ApplicationStatus.EXPIRED,
    null,
      -> buildStartApprovedPremiseReferralAction(data, clock, isWithinOneYear)
  }

  private fun buildStartApprovedPremiseReferralAction(data: DomainData, clock: Clock, isWithinOneYear: Boolean): RuleAction {
    val releaseDate = data.releaseDate ?: error("Release date for crn: ${data.crn} is null")

    if (isWithinOneYear) return RuleAction(START_APPROVED_PREMISE_APPLICATION)
    return buildReferralStartDateAction(releaseDate, clock)
  }

  private fun buildReferralStartDateAction(releaseDate: LocalDate, clock: Clock): RuleAction {
    val dateToStartReferral = releaseDate.minusYears(1)
    val daysUntilReferralMustStart = DAYS.between(LocalDate.now(clock), dateToStartReferral).toInt()

    return when {
      daysUntilReferralMustStart > 1
        -> RuleAction("$START_APPROVED_PREMISE_APPLICATION in $daysUntilReferralMustStart days", true)

      daysUntilReferralMustStart < 1 -> RuleAction(START_APPROVED_PREMISE_APPLICATION)

      else -> RuleAction("$START_APPROVED_PREMISE_APPLICATION in 1 day", true)
    }
  }
}
