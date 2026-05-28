package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.accommodation

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.accommodation.NoNextAccommodationRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData

class NoNextAccommodationRuleTest {
  private val description = "FAIL if candidate has next accommodation"

  @Test
  fun `candidate passes when hasNextAccommodation is false`() {
    val data = buildDomainData(
      nextAccommodations = emptyList(),
    )

    val result = NoNextAccommodationRule().evaluate(data)

    assertThat(result).isEqualTo(RuleResult(description = description, ruleStatus = RuleStatus.PASS))
  }

  @Test
  fun `candidate fails when hasNextAccommodation is true`() {
    val data = buildDomainData(
      nextAccommodations = listOf(buildAccommodationSummaryDto()),
    )

    val result = NoNextAccommodationRule().evaluate(data)

    assertThat(result).isEqualTo(RuleResult(description = description, ruleStatus = RuleStatus.FAIL, failureReason = FailureReason.HAS_NEXT_ACCOMMODATION))
  }

  @Test
  fun `rule has correct description`() {
    assertThat(NoNextAccommodationRule().description)
      .isEqualTo("FAIL if candidate has next accommodation")
  }
}
