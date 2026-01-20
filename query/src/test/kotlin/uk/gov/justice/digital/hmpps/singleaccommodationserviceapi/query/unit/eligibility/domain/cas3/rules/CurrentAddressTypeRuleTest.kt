package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas3.rules

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.accommodationdatadomain.AccommodationType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildAccommodation
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.rules.CurrentAddressTypeRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import java.time.LocalDate
import java.util.stream.Stream

class CurrentAddressTypeRuleTest {

  private val crn = "ABC234"
  private val male = SexCode.M

  @ParameterizedTest
  @MethodSource("provideEligibleAccommodationTypes")
  fun `candidate passes when current accommodation is eligible type`(accommodationType: AccommodationType) {
    val data = DomainData(
      crn = crn,
      tier = TierScore.A1,
      sex = male,
      releaseDate = LocalDate.now().plusMonths(1),
      currentAccommodation = buildAccommodation(type = accommodationType),
    )

    val result = CurrentAddressTypeRule().evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.PASS)
  }

  @ParameterizedTest
  @MethodSource("provideIneligibleAccommodationTypes")
  fun `candidate fails when current accommodation is ineligible type`(accommodationType: AccommodationType) {
    val data = DomainData(
      crn = crn,
      tier = TierScore.A1,
      sex = male,
      releaseDate = LocalDate.now().plusMonths(1),
      currentAccommodation = buildAccommodation(type = accommodationType),
    )

    val result = CurrentAddressTypeRule().evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.FAIL)
  }

  @Test
  fun `candidate fails when current accommodation is null`() {
    val data = DomainData(
      crn = crn,
      tier = TierScore.A1,
      sex = male,
      releaseDate = LocalDate.now().plusMonths(1),
      currentAccommodation = null,
    )

    val result = CurrentAddressTypeRule().evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.FAIL)
  }

  @Test
  fun `rule has correct description`() {
    assertThat(CurrentAddressTypeRule().description)
      .isEqualTo("FAIL if current address is not Approved Premise (CAS1), CAS2, or Prison")
  }

  private companion object {
    @JvmStatic
    fun provideEligibleAccommodationTypes(): Stream<Arguments> = Stream.of(
      Arguments.of(AccommodationType.PRISON),
      Arguments.of(AccommodationType.CAS1),
      Arguments.of(AccommodationType.CAS2),
      Arguments.of(AccommodationType.CAS2V2),
    )

    @JvmStatic
    fun provideIneligibleAccommodationTypes(): Stream<Arguments> = Stream.of(
      Arguments.of(AccommodationType.CAS3),
      Arguments.of(AccommodationType.PRIVATE),
      Arguments.of(AccommodationType.NO_FIXED_ABODE),
    )
  }
}
