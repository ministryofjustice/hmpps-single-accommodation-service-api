package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas3.eligibility

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.eligibility.NextAccommodationRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData

class NextAccommodationRuleTest {

  @Test
  fun `candidate passes when next accommodation is null`() {
    val data = buildDomainData(
      hasNextAccommodation = false,
    )

    val result = NextAccommodationRule().evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.PASS)
  }

  @Test
  fun `candidate fails when next accommodation exists`() {
    val data = buildDomainData(
      hasNextAccommodation = true,
    )

    val result = NextAccommodationRule().evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.FAIL)
  }

  @Test
  fun `rule has correct description`() {
    assertThat(NextAccommodationRule().description)
      .isEqualTo("FAIL if candidate has next accommodation")
  }
}
