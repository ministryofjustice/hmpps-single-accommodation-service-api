package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.common

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.NoNextAccommodationRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData

class NoNextAccommodationRuleTest {
  @Test
  fun `candidate passes when next accommodation is null`() {
    val data = buildDomainData(
      hasNextAccommodation = false,
    )

    val result = NoNextAccommodationRule().evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.PASS)
  }

  @Test
  fun `candidate fails when next accommodation exists`() {
    val data = buildDomainData(
      hasNextAccommodation = true,
    )

    val result = NoNextAccommodationRule().evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.FAIL)
  }

  @Test
  fun `rule has correct description`() {
    assertThat(NoNextAccommodationRule().description)
      .isEqualTo("FAIL if candidate has next accommodation")
  }
}
