package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas3.rules

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.enums.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.enums.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.rules.NoConflictingCas1BookingRule
import java.time.LocalDate

class NoConflictingCas1BookingRuleTest {
  private val crn = "ABC234"
  private val male = SexCode.M
  private val tier = TierScore.A1
  private val description = "FAIL if CAS1 booking exists for upcoming release"

  @ParameterizedTest(name = "{0}")
  @EnumSource(value = Cas1PlacementStatus::class, names = ["UPCOMING", "ARRIVED"])
  fun `CAS1 booking exists so rule fails`(placementStatus: Cas1PlacementStatus) {
    val data = DomainData(
      crn = crn,
      tier = tier,
      sex = male,
      releaseDate = LocalDate.now().plusMonths(5),
      cas1Application = buildCas1Application(
        applicationStatus = Cas1ApplicationStatus.PLACEMENT_ALLOCATED,
        placementStatus = placementStatus,
      ),
    )

    val result = NoConflictingCas1BookingRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.FAIL,
      ),
    )
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(value = Cas1PlacementStatus::class, names = ["DEPARTED", "CANCELLED", "NOT_ARRIVED"])
  fun `CAS1 application is PLACEMENT_ALLOCATED but placement is not active so rule passes`(placementStatus: Cas1PlacementStatus) {
    val data = DomainData(
      crn = crn,
      tier = tier,
      sex = male,
      releaseDate = LocalDate.now().plusMonths(5),
      cas1Application = buildCas1Application(
        applicationStatus = Cas1ApplicationStatus.PLACEMENT_ALLOCATED,
        placementStatus = placementStatus,
      ),
    )

    val result = NoConflictingCas1BookingRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.PASS,
      ),
    )
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(value = Cas1ApplicationStatus::class, mode = EnumSource.Mode.EXCLUDE, names = ["PLACEMENT_ALLOCATED"])
  fun `CAS1 application is not PLACEMENT_ALLOCATED so rule passes`(applicationStatus: Cas1ApplicationStatus) {
    val data = DomainData(
      crn = crn,
      tier = tier,
      sex = male,
      releaseDate = LocalDate.now().plusMonths(5),
      cas1Application = buildCas1Application(
        applicationStatus = applicationStatus,
      ),
    )

    val result = NoConflictingCas1BookingRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.PASS,
      ),
    )
  }

  @Test
  fun `CAS1 application is not present so rule passes`() {
    val data = DomainData(
      crn = crn,
      tier = tier,
      sex = male,
      releaseDate = LocalDate.now().plusMonths(5),
      cas1Application = null,
    )

    val result = NoConflictingCas1BookingRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.PASS,
      ),
    )
  }

  @Test
  fun `rule has correct description`() {
    assertThat(NoConflictingCas1BookingRule().description).isEqualTo(description)
  }
}
