package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.rules

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.RuleAction
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.enums.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import java.time.Clock
import java.time.LocalDate
import java.time.temporal.ChronoUnit.DAYS
import java.time.temporal.ChronoUnit.MONTHS

@Component
class ApplicationSuitabilityRule(
  private val clock: Clock,
) : Cas1SuitabilityRule {
  override val description = "FAIL if candidate does not have a suitable application"
  override val actionable = true
  val actionText = "Start approved premise referral"

  override fun buildAction(data: DomainData): RuleAction {
    if (data.releaseDate == null) {
     return error("Release date for crn: ${data.crn} is null")
    }
    val monthsUntilRelease = MONTHS.between(
      LocalDate.now(clock),
      data.releaseDate,
    )
    val isWithin6Months = monthsUntilRelease < 6

   return if (isWithin6Months) {
     RuleAction(actionText)
    } else {
      val dateToStartReferral = data.releaseDate.minusMonths(6)
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

  override fun evaluate(data: DomainData): RuleResult {
    val suitableStatuses = listOf(
      Cas1ApplicationStatus.AWAITING_ASSESSMENT,
      Cas1ApplicationStatus.UNALLOCATED_ASSESSMENT,
      Cas1ApplicationStatus.ASSESSMENT_IN_PROGRESS,
      Cas1ApplicationStatus.AWAITING_PLACEMENT,
      Cas1ApplicationStatus.PLACEMENT_ALLOCATED,
      Cas1ApplicationStatus.REQUEST_FOR_FURTHER_INFORMATION,
      Cas1ApplicationStatus.PENDING_PLACEMENT_REQUEST,
    )

    val isSuitableApplication = suitableStatuses.contains(data.cas1Application?.applicationStatus)

    val ruleStatus = if (isSuitableApplication) RuleStatus.PASS else RuleStatus.FAIL

    return RuleResult(
      description = description,
      ruleStatus = ruleStatus,
      actionable = actionable,
      potentialAction = this.actionWrapper(ruleStatus, data)
    )
  }
}
