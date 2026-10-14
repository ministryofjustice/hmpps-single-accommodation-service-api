package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas2.eligibility

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.FieldSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildAccommodationTypeEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligibility.CurrentAccommodationTypeIsCas3Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData

class CurrentAccommodationTypeIsCas3RuleTest {
  private val description = "FAIL if individual is in CAS3"

  @Test
  fun `individual is in CAS3 so rule fails`() {
    val data = buildDomainData(
      currentAccommodationTypeEntity = buildAccommodationTypeEntity(
        code = "A17",
      ),
    )

    val result = CurrentAccommodationTypeIsCas3Rule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.FAIL,
        failureReason = FailureReason.IN_CAS3_ACCOMMODATION,
      ),
    )
  }

  @ParameterizedTest
  @FieldSource("nonCas3Codes")
  fun `individual not in CAS3 so rule passes`(code: String) {
    val data = buildDomainData(
      currentAccommodationTypeEntity = buildAccommodationTypeEntity(
        code = code,
      ),
    )

    val result = CurrentAccommodationTypeIsCas3Rule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.PASS,
      ),
    )
  }

  @Test
  fun `individual has no current accommodation so rule passes`() {
    val data = buildDomainData(
      currentAccommodationTypeEntity = null,
    )

    val result = CurrentAccommodationTypeIsCas3Rule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.PASS,
      ),
    )
  }

  companion object {
    @JvmField
    val nonCas3Codes = listOf(
      "A10",
      "A11",
      "A07B",
      "A07A",
      "A14",
      "A13",
      "A01A",
      "A15",
      "A12",
      "A01C",
      "A01D",
      "A04",
      "A03",
      "A02",
      "A16",
      "HMP",
      "A08A",
      "A08C",
      "A08",
    )
  }
}
