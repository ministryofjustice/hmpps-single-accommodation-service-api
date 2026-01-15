package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas1.rules

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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildSex
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.rules.ApplicationSuitabilityRule
import java.time.LocalDate
import java.util.UUID

class ApplicationSuitabilityRuleTest {
  private val crn = "ABC234"
  private val male = buildSex(SexCode.M)
  private val tier = TierScore.A1
  private val description = "FAIL if candidate does not have a suitable application"

  @ParameterizedTest
  @MethodSource("uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas1.rules.ApplicationSuitabilityRuleTest#provideSuitableCas1Applications")
  fun `application is suitable (but not PLACEMENT_ALLOCATED) so rule passes`(cas1Application: Cas1Application) {
    val data = DomainData(
      crn = crn,
      tier = tier,
      sex = male,
      releaseDate = LocalDate.now().plusMonths(5),
      cas1Application = cas1Application
    )

    val result = ApplicationSuitabilityRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.PASS,
      )
    )

  }

  @ParameterizedTest
  @MethodSource("uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas1.rules.ApplicationSuitabilityRuleTest#providePlacementAllocatedCas1Applications")
  fun `application is suitable (PLACEMENT_ALLOCATED) so rule passes`(cas1Application: Cas1Application) {
    val data = DomainData(
      crn = crn,
      tier = tier,
      sex = male,
      releaseDate = LocalDate.now().plusMonths(5),
      cas1Application = cas1Application
    )

    val result = ApplicationSuitabilityRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.PASS,
      )
    )
  }

  @ParameterizedTest
  @MethodSource("uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas1.rules.ApplicationSuitabilityRuleTest#provideUnsuitableCas1Applications")
  fun `application does not have a suitable status so rule fails`(cas1Application: Cas1Application) {
    val data = DomainData(
      crn = crn,
      tier = tier,
      sex = male,
      releaseDate = LocalDate.now().plusMonths(5),
      cas1Application = cas1Application
    )

    val result = ApplicationSuitabilityRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.FAIL,
      )
    )
  }

  @Test
  fun `application is not present so rule fails`() {
    val data = DomainData(
      crn = crn,
      tier = tier,
      sex = male,
      releaseDate = LocalDate.now().plusMonths(5),
      cas1Application = null
    )

    val result = ApplicationSuitabilityRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.FAIL,
      )
    )
  }

  private companion object {

    val suitableStatuses = listOf(
      Cas1ApplicationStatus.AWAITING_ASSESSMENT,
      Cas1ApplicationStatus.UNALLOCATED_ASSESSMENT,
      Cas1ApplicationStatus.ASSESSMENT_IN_PROGRESS,
      Cas1ApplicationStatus.AWAITING_PLACEMENT,
      Cas1ApplicationStatus.REQUEST_FOR_FURTHER_INFORMATION,
      Cas1ApplicationStatus.PENDING_PLACEMENT_REQUEST,
    )

    val unsuitableStatuses = listOf(
      Cas1ApplicationStatus.EXPIRED,
      Cas1ApplicationStatus.INAPPLICABLE,
      Cas1ApplicationStatus.STARTED,
      Cas1ApplicationStatus.REJECTED,
      Cas1ApplicationStatus.WITHDRAWN,
    )

    @JvmStatic
    fun provideSuitableCas1Applications() =
      suitableStatuses.map {
        Arguments.of(
          Cas1Application(
            id = UUID.randomUUID(),
            applicationStatus = it,
            placementStatus = null
          )
        )
      }.stream()

    @JvmStatic
    fun providePlacementAllocatedCas1Applications() =
      Cas1PlacementStatus.entries.map {
        Arguments.of(
          Cas1Application(
            id = UUID.randomUUID(),
            applicationStatus = Cas1ApplicationStatus.PLACEMENT_ALLOCATED,
            placementStatus = it
          )
        )
      }.stream()

    @JvmStatic
    fun provideUnsuitableCas1Applications() =
      unsuitableStatuses.map {
        Arguments.of(
          Cas1Application(
            id = UUID.randomUUID(),
            applicationStatus = it,
            placementStatus = null
          )
        )
      }.stream()
  }
}




