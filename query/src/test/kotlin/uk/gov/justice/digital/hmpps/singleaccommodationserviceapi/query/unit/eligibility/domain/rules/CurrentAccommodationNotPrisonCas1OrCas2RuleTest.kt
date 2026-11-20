package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.rules

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildAccommodationTypeEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.CurrentAccommodationNotPrisonCas1OrCas2Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData

class CurrentAccommodationNotPrisonCas1OrCas2RuleTest {
  private val description = "FAIL if the person is not in CAS1, CAS2 or prison"

  @Test
  fun `person passes when current accommodation is prison type`() {
    val data = buildDomainData(
      currentAccommodationTypeEntity = buildAccommodationTypeEntity(
        isPrison = true,
      ),
    )

    val result = CurrentAccommodationNotPrisonCas1OrCas2Rule().evaluate(data)

    assertThat(result).isEqualTo(RuleResult(description = description, ruleStatus = RuleStatus.PASS))
  }

  @Test
  fun `person passes when current accommodation is cas1 type`() {
    val data = buildDomainData(
      currentAccommodationTypeEntity = buildAccommodationTypeEntity(
        isCas1 = true,
      ),
    )

    val result = CurrentAccommodationNotPrisonCas1OrCas2Rule().evaluate(data)

    assertThat(result).isEqualTo(RuleResult(description = description, ruleStatus = RuleStatus.PASS))
  }

  @Test
  fun `person passes when current accommodation is cas2 type`() {
    val data = buildDomainData(
      currentAccommodationTypeEntity = buildAccommodationTypeEntity(
        isCas2 = true,
      ),
    )

    val result = CurrentAccommodationNotPrisonCas1OrCas2Rule().evaluate(data)

    assertThat(result).isEqualTo(RuleResult(description = description, ruleStatus = RuleStatus.PASS))
  }

  @Test
  fun `person fails when current accommodation is ineligible type`() {
    val data = buildDomainData(
      currentAccommodationTypeEntity = buildAccommodationTypeEntity(
        isCas2 = false,
        isPrison = false,
        isPrivate = false,
      ),
    )

    val result = CurrentAccommodationNotPrisonCas1OrCas2Rule().evaluate(data)

    assertThat(result).isEqualTo(RuleResult(description = description, ruleStatus = RuleStatus.FAIL, failureReason = FailureReason.INVALID_CURRENT_ACCOMMODATION_TYPE))
  }

  @Test
  fun `person fails when current accommodation is null`() {
    val data = buildDomainData(
      currentAccommodationTypeEntity = null,
    )

    val result = CurrentAccommodationNotPrisonCas1OrCas2Rule().evaluate(data)

    assertThat(result).isEqualTo(RuleResult(description = description, ruleStatus = RuleStatus.FAIL, failureReason = FailureReason.INVALID_CURRENT_ACCOMMODATION_TYPE))
  }

  @Test
  fun `rule has correct description`() {
    assertThat(CurrentAccommodationNotPrisonCas1OrCas2Rule().description)
      .isEqualTo(description)
  }
}
