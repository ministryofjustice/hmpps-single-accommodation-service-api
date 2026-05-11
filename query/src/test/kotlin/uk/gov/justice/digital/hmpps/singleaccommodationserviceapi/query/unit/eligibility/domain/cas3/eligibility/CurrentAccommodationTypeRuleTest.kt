package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas3.eligibility

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationTypeDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.eligibility.CurrentAccommodationTypeRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData

class CurrentAccommodationTypeRuleTest {
  private val description = "FAIL if current accommodation is not Approved Premise (CAS1), CAS2, or Prison"

  // TODO turn these into parameterized test once the types are known for certain
  @Test
  fun `candidate passes when current accommodation is eligible type`() {
    val data = buildDomainData(
      currentAccommodation = buildAccommodationSummaryDto(
        type = buildAccommodationTypeDto(
          code = "A02",
        ),
      ),
    )

    val result = CurrentAccommodationTypeRule().evaluate(data)

    assertThat(result).isEqualTo(RuleResult(description = description, ruleStatus = RuleStatus.PASS))
  }

  @Test
  fun `candidate fails when current accommodation is ineligible type`() {
    val data = buildDomainData(
      currentAccommodation = buildAccommodationSummaryDto(
        type = buildAccommodationTypeDto(
          code = "A03",
        ),
      ),
    )

    val result = CurrentAccommodationTypeRule().evaluate(data)

    assertThat(result).isEqualTo(RuleResult(description = description, ruleStatus = RuleStatus.FAIL, failureReason = FailureReason.INVALID_CURRENT_ACCOMMODATION_TYPE))
  }

  @Test
  fun `candidate fails when current accommodation is null`() {
    val data = buildDomainData(
      currentAccommodation = null,
    )

    val result = CurrentAccommodationTypeRule().evaluate(data)

    assertThat(result).isEqualTo(RuleResult(description = description, ruleStatus = RuleStatus.FAIL, failureReason = FailureReason.INVALID_CURRENT_ACCOMMODATION_TYPE))
  }

  @Test
  fun `rule has correct description`() {
    assertThat(CurrentAccommodationTypeRule().description)
      .isEqualTo("FAIL if current accommodation is not Approved Premise (CAS1), CAS2, or Prison")
  }
}
