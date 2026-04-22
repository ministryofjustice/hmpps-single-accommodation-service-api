package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.completion.Cas3CompletionRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.suitability.Cas3SuitabilityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.isWithinOneYear
import java.time.Clock
import java.time.LocalDate

@Component
class Cas3RecentCurrentAccommodationEndDateRule(val clock: Clock) :
  Cas3SuitabilityRule,
  Cas3CompletionRule {
  override val description = "FAIL if CAS3 application is not within 1 year of current accommodation end date"

  override fun evaluate(data: DomainData) = RuleResult(
    description = description,
    ruleStatus = if (isWithinOneYear(
        data.currentAccommodation?.endDate,
        LocalDate.now(clock),
      )
    ) {
      RuleStatus.PASS
    } else {
      RuleStatus.FAIL
    },
  )
}
