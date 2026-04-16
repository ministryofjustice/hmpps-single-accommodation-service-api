package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas3.eligibility

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationArrangementType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.eligibility.CurrentAddressTypeRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import java.time.LocalDate

class CurrentAddressTypeRuleTest {

  private val crn = "ABC234"
  private val male = SexCode.M

  @ParameterizedTest(name = "{0}")
  @EnumSource(value = AccommodationArrangementType::class, names = ["PRISON", "CAS1", "CAS2", "CAS2V2"])
  fun `candidate passes when current accommodation is eligible type`(accommodationType: AccommodationArrangementType) {
    val data = buildDomainData(
      crn = crn,
      tierScore = TierScore.A1,
      sex = male,
      releaseDate = LocalDate.now().plusMonths(1),
      currentAccommodationArrangementType = accommodationType,
    )

    val result = CurrentAddressTypeRule().evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.PASS)
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(value = AccommodationArrangementType::class, names = ["CAS3", "PRIVATE", "NO_FIXED_ABODE"])
  fun `candidate fails when current accommodation is ineligible type`(accommodationType: AccommodationArrangementType) {
    val data = buildDomainData(
      crn = crn,
      tierScore = TierScore.A1,
      sex = male,
      releaseDate = LocalDate.now().plusMonths(1),
      currentAccommodationArrangementType = accommodationType,
    )

    val result = CurrentAddressTypeRule().evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.FAIL)
  }

  @Test
  fun `candidate fails when current accommodation is null`() {
    val data = buildDomainData(
      crn = crn,
      tierScore = TierScore.A1,
      sex = male,
      releaseDate = LocalDate.now().plusMonths(1),
      currentAccommodationArrangementType = null,
    )

    val result = CurrentAddressTypeRule().evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.FAIL)
  }

  @Test
  fun `rule has correct description`() {
    assertThat(CurrentAddressTypeRule().description)
      .isEqualTo("FAIL if current address is not Approved Premise (CAS1), CAS2, or Prison")
  }
}
