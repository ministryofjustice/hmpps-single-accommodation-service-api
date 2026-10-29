package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas2.eligible.rules

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildAccommodationTypeEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligible.rules.CurrentAccommodationTypeIsCas3Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData

class CurrentAccommodationTypeIsCas3RuleTest {
  private val description = "FAIL if person is in CAS3"

  @Test
  fun `person is in CAS3 so rule fails`() {
    val data = buildDomainData(
      currentAccommodationTypeEntity = buildAccommodationTypeEntity(
        isCas3 = true,
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

  @Test
  fun `person not in CAS3 so rule passes`() {
    val data = buildDomainData(
      currentAccommodationTypeEntity = buildAccommodationTypeEntity(
        isCas3 = false,
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
  fun `person has no current accommodation so rule passes`() {
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
}
