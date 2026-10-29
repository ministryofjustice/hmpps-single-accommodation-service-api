package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas2.eligibility

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.FieldSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildAccommodationTypeEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligibility.CurrentAccommodationTypeIsCommunityRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData

class CurrentAccommodationTypeIsCommunityRuleTest {
  private val description = "FAIL if person is in the community"

  @ParameterizedTest
  @FieldSource("communityCodes")
  fun `person is in community so rule fails`(code: String) {
    val data = buildDomainData(
      currentAccommodationTypeEntity = buildAccommodationTypeEntity(
        isPrison = false,
        isCas1 = false,
        isCas2 = false,
        code = code,
      ),
    )

    val result = CurrentAccommodationTypeIsCommunityRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.FAIL,
        failureReason = FailureReason.IN_COMMUNITY,
      ),
    )
  }

  @Test
  fun `person has no current accommodation so rule fails`() {
    val data = buildDomainData(
      currentAccommodationTypeEntity = null,
    )

    val result = CurrentAccommodationTypeIsCommunityRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.FAIL,
        failureReason = FailureReason.IN_COMMUNITY,
      ),
    )
  }

  @Test
  fun `person is in prison so rule passes`() {
    val data = buildDomainData(
      currentAccommodationTypeEntity = buildAccommodationTypeEntity(
        isPrison = true,
        isCas1 = false,
        isCas2 = false,
        code = "HMP",
      ),
    )

    val result = CurrentAccommodationTypeIsCommunityRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.PASS,
      ),
    )
  }

  @Test
  fun `person is in CAS1 so rule passes`() {
    val data = buildDomainData(
      currentAccommodationTypeEntity = buildAccommodationTypeEntity(
        isPrison = false,
        isCas1 = true,
        isCas2 = false,
        code = "A02",
      ),
    )

    val result = CurrentAccommodationTypeIsCommunityRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.PASS,
      ),
    )
  }

  @ParameterizedTest
  @FieldSource("cas2Codes")
  fun `person is in CAS2 so rule passes`(code: String) {
    val data = buildDomainData(
      currentAccommodationTypeEntity = buildAccommodationTypeEntity(
        isPrison = false,
        isCas1 = false,
        isCas2 = true,
        isCas3 = false,
        code = code,
      ),
    )

    val result = CurrentAccommodationTypeIsCommunityRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.PASS,
      ),
    )
  }

  @Test
  fun `person is in CAS3 so rule passes`() {
    val data = buildDomainData(
      currentAccommodationTypeEntity = buildAccommodationTypeEntity(
        code = "A17",
        isPrison = false,
        isCas1 = false,
        isCas2 = false,
        isCas3 = true,
      ),
    )

    val result = CurrentAccommodationTypeIsCommunityRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.PASS,
      ),
    )
  }

  companion object {
    @JvmField
    val communityCodes = listOf(
      "A16",
      "A07B",
      "A07A",
      "A14",
      "A13",
      "A08A",
      "A08C",
      "A08",
      "A01A",
      "A15",
      "A12",
      "A01C",
      "A01D",
      "A04",
      "A03",
    )

    @JvmField
    val cas2Codes = listOf(
      "A10",
      "A11",
    )
  }
}
