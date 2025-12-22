package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.cas1.rules

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.enums.RuleStatus
import java.time.Clock
import java.time.LocalDate
import java.time.temporal.ChronoUnit.DAYS
import java.time.temporal.ChronoUnit.MONTHS

class WithinSixMonthsOfReleaseRule(
  private val clock: Clock = Clock.systemDefaultZone(),
) : Rule {
  override val description = "FAIL if candidate is within 6 months of release date"
  override val actionable = true
  val actionText = "Start approved premise referral"

  fun buildAction(isCandidateWithin6Months: Boolean, data: DomainData) = if (isCandidateWithin6Months) {
    actionText
  } else {
    val dateToStartReferral = data.releaseDate!!.minusMonths(6)
    val daysUntilReferralMustStart = DAYS.between(LocalDate.now(clock), dateToStartReferral).toInt()
    if (daysUntilReferralMustStart > 1) {
      "$actionText in $daysUntilReferralMustStart days"
    } else if (daysUntilReferralMustStart < 1) {
      actionText
    } else {
      "$actionText in 1 day"
    }
  }

  override fun evaluate(data: DomainData): RuleResult {
    if (data.releaseDate == null) {
      return RuleResult(
        description = description,
        ruleStatus = RuleStatus.PASS,
        actionable = false,
      )
    }

    val monthsUntilRelease = MONTHS.between(
      LocalDate.now(clock),
      data.releaseDate.toLocalDate(),
    )

    val isCandidateWithin6Months = monthsUntilRelease < 6

    return RuleResult(
      description = description,
      ruleStatus = if (isCandidateWithin6Months) RuleStatus.FAIL else RuleStatus.PASS,
      actionable = actionable,
      potentialAction = buildAction(isCandidateWithin6Months, data),
    )
  }
}
