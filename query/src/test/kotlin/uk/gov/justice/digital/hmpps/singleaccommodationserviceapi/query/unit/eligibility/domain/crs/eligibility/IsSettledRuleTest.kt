package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.crs.eligibility

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildAccommodationTypeEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationSettledType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.eligibility.IsSettledRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData

class IsSettledRuleTest {
  private val description = "FAIL if candidate is settled"

  @Test
  fun `current accommodation is SETTLED so rule fails`() {
    val data = buildDomainData(
      currentAccommodationTypeEntity = buildAccommodationTypeEntity(
        settledType = AccommodationSettledType.SETTLED,
      ),
    )

    val result = IsSettledRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.FAIL,
        failureReason = FailureReason.IS_SETTLED,
      ),
    )
  }

  @Test
  fun `current accommodation is TRANSIENT so rule fails`() {
    val data = buildDomainData(
      currentAccommodationTypeEntity = buildAccommodationTypeEntity(
        settledType = AccommodationSettledType.TRANSIENT,
      ),
    )

    val result = IsSettledRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.PASS,
      ),
    )
  }

  @Test
  fun `current accommodation is missing so rule passes`() {
    val data = buildDomainData(
      currentAccommodationTypeEntity = null,
    )

    val result = IsSettledRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.PASS,
      ),
    )
  }
}
