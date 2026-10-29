package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas2.eligibility

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildAccommodationTypeEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligibility.CurrentAccommodationTypeIsCas2Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData

class CurrentAccommodationTypeIsCas2RuleTest {
  private val description = "FAIL if individual is in CAS2"

  @Test
  fun `individual is in CAS2 so rule fails`() {
    val data = buildDomainData(
      currentAccommodationTypeEntity = buildAccommodationTypeEntity(
        isCas2 = true,
      ),
    )

    val result = CurrentAccommodationTypeIsCas2Rule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.FAIL,
        failureReason = FailureReason.IN_CAS2_ACCOMMODATION,
      ),
    )
  }

  @Test
  fun `individual not in CAS2 so rule passes`() {
    val data = buildDomainData(
      currentAccommodationTypeEntity = buildAccommodationTypeEntity(
        isCas2 = false,
      ),
    )

    val result = CurrentAccommodationTypeIsCas2Rule().evaluate(data)

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

    val result = CurrentAccommodationTypeIsCas2Rule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.PASS,
      ),
    )
  }
}
