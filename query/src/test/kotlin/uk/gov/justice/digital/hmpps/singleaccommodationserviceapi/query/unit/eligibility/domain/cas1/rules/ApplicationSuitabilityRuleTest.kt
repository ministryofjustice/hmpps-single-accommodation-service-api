package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas1.rules

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.RuleAction
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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.utils.MutableClock
import java.time.LocalDate
import java.util.UUID

class ApplicationSuitabilityRuleTest {
  private val crn = "ABC234"
  private val male = buildSex(SexCode.M)
  private val clock = MutableClock()
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

    val result = ApplicationSuitabilityRule(clock).evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.PASS,
        actionable = true,
        potentialAction = null,
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

    val result = ApplicationSuitabilityRule(clock).evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.PASS,
        actionable = true,
        potentialAction = null,
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

    val result = ApplicationSuitabilityRule(clock).evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.FAIL,
        actionable = true,
        potentialAction = RuleAction("Start approved premise referral"),
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

    val result = ApplicationSuitabilityRule(clock).evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.FAIL,
        actionable = true,
        potentialAction = RuleAction("Start approved premise referral"),
      )
    )
  }

  @Nested
  inner class BuildAction {
    @Test
    fun `Build action when release date is 3 days in future`() {
      val releaseDate = LocalDate.parse("2026-12-31")
      clock.setNow(releaseDate.minusDays(3))
      val data = DomainData(
        crn = crn,
        tier = TierScore.A1,
        sex = male,
        releaseDate = releaseDate,
      )
      val result = ApplicationSuitabilityRule(clock).buildAction(data)
      val expectedResult = RuleAction("Start approved premise referral")
      assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `Build action when release date is 7 months in future`() {
      val releaseDate = LocalDate.parse("2026-12-31")
      clock.setNow(releaseDate.minusMonths(7))
      val data = DomainData(
        crn = crn,
        tier = TierScore.A1,
        sex = male,
        releaseDate = releaseDate,
      )
      val result = ApplicationSuitabilityRule(clock).buildAction(data)
      val expectedResult = RuleAction("Start approved premise referral in 30 days", true)
      assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `Build action when release date is 6 months in future`() {
      val releaseDate = LocalDate.parse("2026-12-31")
      clock.setNow(releaseDate.minusMonths(6))
      val data = DomainData(
        crn = crn,
        tier = TierScore.A1,
        sex = male,
        releaseDate = releaseDate,
      )
      val result = ApplicationSuitabilityRule(clock).buildAction(data)
      val expectedResult = RuleAction("Start approved premise referral")
      assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `Build action when release date is 6 months and 1 day in future`() {
      val releaseDate = LocalDate.parse("2026-12-31")
      clock.setNow(releaseDate.minusMonths(6).minusDays(1))
      val data = DomainData(
        crn = crn,
        tier = TierScore.A1,
        sex = male,
        releaseDate = releaseDate,
      )
      val result = ApplicationSuitabilityRule(clock).buildAction(data)
      val expectedResult = RuleAction("Start approved premise referral in 1 day", true)
      assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `Build action when release date is 6 months and 2 days in future`() {
      val releaseDate = LocalDate.parse("2026-07-01")
      clock.setNow(releaseDate.minusMonths(6).minusDays(2))
      val data = DomainData(
        crn = crn,
        tier = TierScore.A1,
        sex = male,
        releaseDate = releaseDate,
      )
      val result = ApplicationSuitabilityRule(clock).buildAction(data)
      val expectedResult = RuleAction("Start approved premise referral in 2 days", true)
      assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `Build action when release date null and error`() {
      val data = DomainData(
        crn = crn,
        tier = TierScore.A1,
        sex = male,
        releaseDate = null,
      )
      assertThatThrownBy { ApplicationSuitabilityRule(clock).buildAction(data) }
        .isInstanceOf(IllegalStateException::class.java)
        .hasMessageContaining("Release date for crn: ABC234 is null")
    }
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




