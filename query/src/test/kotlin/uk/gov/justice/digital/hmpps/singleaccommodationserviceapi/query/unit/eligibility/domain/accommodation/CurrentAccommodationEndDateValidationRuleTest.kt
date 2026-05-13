package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.accommodation

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.accommodation.CurrentAccommodationEndDateValidationRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import java.time.LocalDate

class CurrentAccommodationEndDateValidationRuleTest {
  private val description = "FAIL if candidate has no current accommodation end date"

  @Test
  fun `candidate passes if current accommodation end date is present`() {
    val data = buildDomainData(
      currentAccommodation = buildAccommodationSummaryDto(endDate = LocalDate.now().plusYears(1)),
    )

    val result = CurrentAccommodationEndDateValidationRule().evaluate(data)

    assertThat(result).isEqualTo(RuleResult(description = description, ruleStatus = RuleStatus.PASS))
  }

  @Test
  fun `candidate fails if current accommodation end date is missing`() {
    val data = buildDomainData(
      currentAccommodation = null,
    )

    val result = CurrentAccommodationEndDateValidationRule().evaluate(data)

    assertThat(result).isEqualTo(RuleResult(description = description, ruleStatus = RuleStatus.FAIL, failureReason = FailureReason.NO_CURRENT_ACCOMMODATION_END_DATE))
  }

  @Test
  fun `rule has correct description`() {
    val result = CurrentAccommodationEndDateValidationRule().description
    assertThat(result).isEqualTo("FAIL if candidate has no current accommodation end date")
  }
}
