package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas1.eligibility

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildAccommodationTypeEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.eligibility.CurrentAccommodationIsPrisonOrCas1Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData

class CurrentAccommodationIsPrisonOrCas1RuleTest {
  private val rule = CurrentAccommodationIsPrisonOrCas1Rule()
  private val description = "FAIL if person is not in prison or CAS1 accommodation"

  @ParameterizedTest
  @CsvSource(
    "true, false, PASS",
    "false, true, PASS",
    "true, true, PASS",
    "false, false, FAIL",
  )
  fun `eligibility uses the prison and CAS1 accommodation flags`(
    isPrison: Boolean,
    isCas1: Boolean,
    expectedStatus: RuleStatus,
  ) {
    val data = buildDomainData(
      currentAccommodationTypeEntity = buildAccommodationTypeEntity(
        isPrison = isPrison,
        isCas1 = isCas1,
      ),
    )

    assertThat(rule.evaluate(data)).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = expectedStatus,
        failureReason = if (expectedStatus == RuleStatus.FAIL) FailureReason.NOT_IN_PRISON_OR_CAS1_ACCOMMODATION else null,
      ),
    )
  }

  @ParameterizedTest
  @ValueSource(strings = ["A10", "A17"])
  fun `CAS2 and CAS3 accommodation do not qualify`(code: String) {
    val data = buildDomainData(
      currentAccommodationTypeEntity = buildAccommodationTypeEntity(
        code = code,
        isCas2 = code == "A10",
        isCas3 = code == "A17",
      ),
    )

    assertThat(rule.evaluate(data)).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.FAIL,
        failureReason = FailureReason.NOT_IN_PRISON_OR_CAS1_ACCOMMODATION,
      ),
    )
  }

  @Test
  fun `missing current accommodation type fails`() {
    val data = buildDomainData(currentAccommodationTypeEntity = null)

    assertThat(rule.evaluate(data)).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.FAIL,
        failureReason = FailureReason.NOT_IN_PRISON_OR_CAS1_ACCOMMODATION,
      ),
    )
  }
}
