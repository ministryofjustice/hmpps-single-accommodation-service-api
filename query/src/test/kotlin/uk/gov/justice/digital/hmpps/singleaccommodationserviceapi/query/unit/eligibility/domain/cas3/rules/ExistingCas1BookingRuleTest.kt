package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas3.rules

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.enums.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.enums.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildSex
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.rules.ExistingCas1BookingRule
import java.time.LocalDate

class ExistingCas1BookingRuleTest {
  private val crn = "ABC234"
  private val male = SexCode.M
  private val tier = TierScore.A1
  private val description = "FAIL if CAS1 booking exists for upcoming release"

  @ParameterizedTest
  @MethodSource("uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas3.rules.ExistingCas1BookingRuleTest#provideCompleteCas1Bookings")
  fun `CAS1 booking exists so rule fails`(cas1Application: Cas1Application) {
    val data = DomainData(
      crn = crn,
      tier = tier,
      sex = male,
      releaseDate = LocalDate.now().plusMonths(5),
      cas1Application = cas1Application
    )

    val result = ExistingCas1BookingRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.FAIL,
      )
    )
  }

  @ParameterizedTest
  @MethodSource("uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas3.rules.ExistingCas1BookingRuleTest#provideIncompletePlacementStatuses")
  fun `CAS1 application is PLACEMENT_ALLOCATED but placement is not active so rule passes`(cas1Application: Cas1Application) {
    val data = DomainData(
      crn = crn,
      tier = tier,
      sex = male,
      releaseDate = LocalDate.now().plusMonths(5),
      cas1Application = cas1Application
    )

    val result = ExistingCas1BookingRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.PASS,
      )
    )
  }

  @ParameterizedTest
  @MethodSource("uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas3.rules.ExistingCas1BookingRuleTest#provideNonPlacementAllocatedStatuses")
  fun `CAS1 application is not PLACEMENT_ALLOCATED so rule passes`(cas1Application: Cas1Application) {
    val data = DomainData(
      crn = crn,
      tier = tier,
      sex = male,
      releaseDate = LocalDate.now().plusMonths(5),
      cas1Application = cas1Application
    )

    val result = ExistingCas1BookingRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.PASS,
      )
    )
  }

  @Test
  fun `CAS1 application is not present so rule passes`() {
    val data = DomainData(
      crn = crn,
      tier = tier,
      sex = male,
      releaseDate = LocalDate.now().plusMonths(5),
      cas1Application = null
    )

    val result = ExistingCas1BookingRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.PASS,
      )
    )
  }

  @Test
  fun `rule has correct description`() {
    assertThat(ExistingCas1BookingRule().description).isEqualTo(description)
  }

  private companion object {

    val completePlacementStatuses = listOf(
      Cas1PlacementStatus.UPCOMING,
      Cas1PlacementStatus.ARRIVED,
    )

    val incompletePlacementStatuses = listOf(
      Cas1PlacementStatus.DEPARTED,
      Cas1PlacementStatus.CANCELLED,
      Cas1PlacementStatus.NOT_ARRIVED,
    )

    val nonPlacementAllocatedStatuses = listOf(
      Cas1ApplicationStatus.AWAITING_ASSESSMENT,
      Cas1ApplicationStatus.UNALLOCATED_ASSESSMENT,
      Cas1ApplicationStatus.ASSESSMENT_IN_PROGRESS,
      Cas1ApplicationStatus.AWAITING_PLACEMENT,
      Cas1ApplicationStatus.PENDING_PLACEMENT_REQUEST,
      Cas1ApplicationStatus.REQUEST_FOR_FURTHER_INFORMATION,
      Cas1ApplicationStatus.EXPIRED,
      Cas1ApplicationStatus.INAPPLICABLE,
      Cas1ApplicationStatus.STARTED,
      Cas1ApplicationStatus.REJECTED,
      Cas1ApplicationStatus.WITHDRAWN,
    )

    @JvmStatic
    fun provideCompleteCas1Bookings() =
      completePlacementStatuses.map {
        Arguments.of(
          buildCas1Application(
            applicationStatus = Cas1ApplicationStatus.PLACEMENT_ALLOCATED,
            placementStatus = it
          )
        )
      }.stream()

    @JvmStatic
    fun provideIncompletePlacementStatuses() =
      incompletePlacementStatuses.map {
        Arguments.of(
          buildCas1Application(
            applicationStatus = Cas1ApplicationStatus.PLACEMENT_ALLOCATED,
            placementStatus = it
          )
        )
      }.stream()

    @JvmStatic
    fun provideNonPlacementAllocatedStatuses() =
      nonPlacementAllocatedStatuses.map {
        Arguments.of(
          buildCas1Application(
            applicationStatus = it,
            placementStatus = null
          )
        )
      }.stream()
  }
}
