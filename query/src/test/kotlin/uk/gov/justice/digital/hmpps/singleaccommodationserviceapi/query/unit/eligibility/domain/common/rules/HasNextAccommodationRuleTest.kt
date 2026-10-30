package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.common.rules

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.rules.HasNextAccommodationRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData

class HasNextAccommodationRuleTest {
  private val description = "FAIL if person has next accommodation"

  @Test
  fun `person passes when hasNextAccommodation is false`() {
    val data = buildDomainData(
      nextAccommodations = emptyList(),
    )

    val result = HasNextAccommodationRule().evaluate(data)

    assertThat(result).isEqualTo(RuleResult(description = description, ruleStatus = RuleStatus.PASS))
  }

  @Test
  fun `person fails when hasNextAccommodation is true`() {
    val data = buildDomainData(
      nextAccommodations = listOf(buildAccommodationSummaryDto()),
    )

    val result = HasNextAccommodationRule().evaluate(data)

    assertThat(result).isEqualTo(RuleResult(description = description, ruleStatus = RuleStatus.FAIL, failureReason = FailureReason.HAS_NEXT_ACCOMMODATION))
  }

  @Test
  fun `rule has correct description`() {
    assertThat(HasNextAccommodationRule().description)
      .isEqualTo("FAIL if person has next accommodation")
  }
}
