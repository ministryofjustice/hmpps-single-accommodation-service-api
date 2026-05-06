package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.dtr.eligibility

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationTypeDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.eligibility.CurrentAddressTypeNotPrivateRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData

class CurrentAddressTypeNotPrivateRuleTest {
  private val description = "FAIL if current address is Private"

  // TODO turn these into parameterized test once the types are known for certain
  @Test
  fun `candidate passes when current address is not private`() {
    val data = buildDomainData(
      currentAccommodation = buildAccommodationSummaryDto(
        type = buildAccommodationTypeDto(
          code = "A03",
        ),
      ),
    )

    val result = CurrentAddressTypeNotPrivateRule().evaluate(data)

    assertThat(result).isEqualTo(RuleResult(description = description, ruleStatus = RuleStatus.PASS))
  }

  @Test
  fun `candidate fails when current address is private`() {
    val data = buildDomainData(
      currentAccommodation = buildAccommodationSummaryDto(
        type = buildAccommodationTypeDto(
          code = "A01A",
        ),
      ),
    )

    val result = CurrentAddressTypeNotPrivateRule().evaluate(data)

    assertThat(result).isEqualTo(RuleResult(description = description, ruleStatus = RuleStatus.FAIL, failureReason = FailureReason.CURRENT_ADDRESS_IS_PRIVATE))
  }

  @Test
  fun `candidate passes when current address is missing`() {
    val data = buildDomainData(
      currentAccommodation = null,
    )

    val result = CurrentAddressTypeNotPrivateRule().evaluate(data)

    assertThat(result).isEqualTo(RuleResult(description = description, ruleStatus = RuleStatus.PASS))
  }

  @Test
  fun `rule has correct description`() {
    assertThat(CurrentAddressTypeNotPrivateRule().description)
      .isEqualTo("FAIL if current address is Private")
  }
}
