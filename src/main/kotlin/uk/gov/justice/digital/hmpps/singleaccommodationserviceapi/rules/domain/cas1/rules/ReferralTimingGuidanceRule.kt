package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.cas1.rules

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.rules.domain.RuleStatus
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit.DAYS
import java.time.temporal.ChronoUnit.MONTHS

class ReferralTimingGuidanceRule : Rule {
  override val description = "FAIL if candidate is within 6 months of release date"
  override val isGuidance = true
  val actionText = "Start approved premise referral"

  fun buildAction(isCandidateWithin6Months: Boolean, data: DomainData) = if (isCandidateWithin6Months) {
    actionText
  } else {
    val dateToStartReferral = data.releaseDate.minusMonths(6)
    val daysUntilReferralMustStart = DAYS.between(OffsetDateTime.now().toLocalDate(), dateToStartReferral).toInt()
    if (daysUntilReferralMustStart > 1) {
      "$actionText in $daysUntilReferralMustStart days"
    } else if (daysUntilReferralMustStart < 1) {
      actionText
    } else {
      "$actionText in 1 day"
    }
  }

  override fun evaluate(data: DomainData): RuleResult {
    val monthsUntilRelease = MONTHS.between(
      OffsetDateTime.now().toLocalDate(),
      data.releaseDate.toLocalDate(),
    )

    val isCandidateWithin6Months = monthsUntilRelease < 6

    return RuleResult(
      description = description,
      ruleStatus = if (isCandidateWithin6Months) RuleStatus.FAIL else RuleStatus.PASS,
      isGuidance = isGuidance,
      potentialAction = buildAction(isCandidateWithin6Months, data),
    )
  }
}
