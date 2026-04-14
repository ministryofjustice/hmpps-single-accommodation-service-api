package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas3.eligibility

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.eligibility.CurrentAccommodationTypeRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildCurrentAccommodation
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData

class CurrentAccommodationTypeRuleTest {

  @Test
  fun `candidate passes when current accommodation is eligible type`() {
    val data = buildDomainData(
      currentAccommodation = buildCurrentAccommodation(isPrisonCas1Cas2OrCas2v2 = true),
    )

    val result = CurrentAccommodationTypeRule().evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.PASS)
  }

  @Test
  fun `candidate fails when current accommodation is ineligible type`() {
    val data = buildDomainData(
      currentAccommodation = buildCurrentAccommodation(isPrisonCas1Cas2OrCas2v2 = false),
    )

    val result = CurrentAccommodationTypeRule().evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.FAIL)
  }

  @Test
  fun `candidate fails when current accommodation is null`() {
    val data = buildDomainData(
      currentAccommodation = null,
    )

    val result = CurrentAccommodationTypeRule().evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.FAIL)
  }

  @Test
  fun `rule has correct description`() {
    assertThat(CurrentAccommodationTypeRule().description)
      .isEqualTo("FAIL if current accommodation is not Approved Premise (CAS1), CAS2, or Prison")
  }
}
