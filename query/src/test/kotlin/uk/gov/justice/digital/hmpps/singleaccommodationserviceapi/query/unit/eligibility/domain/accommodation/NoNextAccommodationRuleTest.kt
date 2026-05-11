package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.accommodation

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.accommodation.NoNextAccommodationRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData

class NoNextAccommodationRuleTest {
  @Test
  fun `candidate passes when hasNextAccommodation is false`() {
    val data = buildDomainData(
      nextAccommodation = null,
    )

    val result = NoNextAccommodationRule().evaluate(data)

    Assertions.assertThat(result.ruleStatus).isEqualTo(RuleStatus.PASS)
  }

  @Test
  fun `candidate fails when hasNextAccommodation is true`() {
    val data = buildDomainData(
      nextAccommodation = buildAccommodationSummaryDto(),
    )

    val result = NoNextAccommodationRule().evaluate(data)

    Assertions.assertThat(result.ruleStatus).isEqualTo(RuleStatus.FAIL)
  }

  @Test
  fun `rule has correct description`() {
    Assertions.assertThat(NoNextAccommodationRule().description)
      .isEqualTo("FAIL if candidate has next accommodation")
  }
}
