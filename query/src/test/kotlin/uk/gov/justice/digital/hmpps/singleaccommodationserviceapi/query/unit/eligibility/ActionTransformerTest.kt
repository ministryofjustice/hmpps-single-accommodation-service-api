package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility

import org.junit.jupiter.api.Test
import org.junit.jupiter.params.provider.Arguments
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.RuleAction
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.enums.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.enums.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildSex
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.buildAction
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.utils.MutableClock
import java.time.LocalDate
import java.util.UUID
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class ActionTransformerTest {
  private val crn = "ABC234"
  private val male = buildSex(SexCode.M)
  private val clock = MutableClock()
  private val tier = TierScore.A1

    @Test
    fun `Build action when release date is 3 days in future and no application`() {
      val releaseDate = LocalDate.parse("2026-12-31")
      clock.setNow(releaseDate.minusDays(3))
      val data = DomainData(
        crn = crn,
        tier = TierScore.A1,
        sex = male,
        releaseDate = releaseDate,
      )
      val result = buildAction(data, clock)
      val expectedResult = RuleAction("Start approved premise referral")
      assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `Build action when release date is 13 months in future and no application`() {
      val releaseDate = LocalDate.parse("2026-12-31")
      clock.setNow(releaseDate.minusMonths(13))
      val data = DomainData(
        crn = crn,
        tier = TierScore.A1,
        sex = male,
        releaseDate = releaseDate,
      )
      val result = buildAction(data, clock)
      val expectedResult = RuleAction("Start approved premise referral in 31 days", true)
      assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `Build action when release date is 1 year in future and no application`() {
      val releaseDate = LocalDate.parse("2026-12-31")
      clock.setNow(releaseDate.minusYears(1))
      val data = DomainData(
        crn = crn,
        tier = TierScore.A1,
        sex = male,
        releaseDate = releaseDate,
      )
      val result = buildAction(data, clock)
      val expectedResult = RuleAction("Start approved premise referral")
      assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `Build action when release date is 1 year and 1 day in future and no application`() {
      val releaseDate = LocalDate.parse("2026-12-31")
      clock.setNow(releaseDate.minusYears(1).minusDays(1))
      val data = DomainData(
        crn = crn,
        tier = TierScore.A1,
        sex = male,
        releaseDate = releaseDate,
      )
      val result = buildAction(data, clock)
      val expectedResult = RuleAction("Start approved premise referral in 1 day", true)
      assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `Build action when release date is 1 year and 2 days in future and no application`() {
      val releaseDate = LocalDate.parse("2026-07-01")
      clock.setNow(releaseDate.minusYears(1).minusDays(2))
      val data = DomainData(
        crn = crn,
        tier = TierScore.A1,
        sex = male,
        releaseDate = releaseDate,
      )
      val result = buildAction(data, clock)
      val expectedResult = RuleAction("Start approved premise referral in 2 days", true)
      assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `Build action when release date null and error and no application`() {
      val data = DomainData(
        crn = crn,
        tier = TierScore.A1,
        sex = male,
        releaseDate = null,
      )
      assertThatThrownBy { buildAction(data, clock) }
        .isInstanceOf(IllegalStateException::class.java)
        .hasMessageContaining("Release date for crn: ABC234 is null")
    }

  @ParameterizedTest
  @MethodSource("uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.ActionTransformerTest#provideUncompletedPlacementAllocatedCas1Applications")
  fun `Build action when application is suitable (PLACEMENT_ALLOCATED) but not completed`(cas1Application: Cas1Application) {
    val data = DomainData(
      crn = crn,
      tier = tier,
      sex = male,
      releaseDate = LocalDate.now().plusMonths(5),
      cas1Application = cas1Application
    )

    val result = buildAction(data, clock)

    assertThat(result).isEqualTo(RuleAction("Create Placement"))
  }

  @ParameterizedTest
  @MethodSource("uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.ActionTransformerTest#provideSuitableCreatePlacementCas1Applications")
  fun `Build action when application is suitable (not PLACEMENT_ALLOCATED) but not completed and needs to create a placement`(cas1Application: Cas1Application) {
    val data = DomainData(
      crn = crn,
      tier = tier,
      sex = male,
      releaseDate = LocalDate.now().plusMonths(5),
      cas1Application = cas1Application
    )

    val result = buildAction(data, clock)

    assertThat(result).isEqualTo(RuleAction("Create Placement"))
  }

  @ParameterizedTest
  @MethodSource("uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.ActionTransformerTest#provideSuitableAssessmentCas1Applications")
  fun `Build action when application is suitable (not PLACEMENT_ALLOCATED) but not completed and needs to await assessment`(cas1Application: Cas1Application) {
    val data = DomainData(
      crn = crn,
      tier = tier,
      sex = male,
      releaseDate = LocalDate.now().plusMonths(5),
      cas1Application = cas1Application
    )

    val result = buildAction(data, clock)

    assertThat(result).isEqualTo(RuleAction("Await Assessment", true))
  }

  @Test
  fun `Build action when application is REQUEST_FOR_FURTHER_INFORMATION`() {
    val data = DomainData(
      crn = crn,
      tier = tier,
      sex = male,
      releaseDate = LocalDate.now().plusMonths(5),
      cas1Application = Cas1Application(
        id = UUID.randomUUID(),
        applicationStatus = Cas1ApplicationStatus.REQUEST_FOR_FURTHER_INFORMATION,
        placementStatus = null
      )
    )

    val result = buildAction(data, clock)

    assertThat(result).isEqualTo(RuleAction("Provide Information"))
  }

  @ParameterizedTest
  @MethodSource("uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.ActionTransformerTest#provideUnsuitableCas1Applications")
  fun `Build action when application is not suitable`(cas1Application: Cas1Application) {

  val data = DomainData(
      crn = crn,
      tier = tier,
      sex = male,
      releaseDate = LocalDate.now().plusMonths(5),
      cas1Application = cas1Application
    )

    val result = buildAction(data, clock)

    assertThat(result).isEqualTo(RuleAction("Start approved premise referral"))
  }

  private companion object {

    val uncompletedPlacementStatuses = listOf(
      Cas1PlacementStatus.DEPARTED,
      Cas1PlacementStatus.CANCELLED,
      Cas1PlacementStatus.NOT_ARRIVED,
    )

    val suitableCreatePlacementStatuses = listOf(
      Cas1ApplicationStatus.AWAITING_PLACEMENT,
      Cas1ApplicationStatus.PENDING_PLACEMENT_REQUEST,
    )

    val suitableAssessmentStatuses = listOf(
      Cas1ApplicationStatus.AWAITING_ASSESSMENT,
      Cas1ApplicationStatus.UNALLOCATED_ASSESSMENT,
      Cas1ApplicationStatus.ASSESSMENT_IN_PROGRESS,
    )

    val unsuitableStatuses = listOf(
      Cas1ApplicationStatus.EXPIRED,
      Cas1ApplicationStatus.INAPPLICABLE,
      Cas1ApplicationStatus.STARTED,
      Cas1ApplicationStatus.REJECTED,
      Cas1ApplicationStatus.WITHDRAWN,
    )

    @JvmStatic
    fun provideSuitableCreatePlacementCas1Applications() =
      suitableCreatePlacementStatuses.map {
        Arguments.of(
          Cas1Application(
            id = UUID.randomUUID(),
            applicationStatus = it,
            placementStatus = null
          )
        )
      }.stream()

    @JvmStatic
    fun provideSuitableAssessmentCas1Applications() =
      suitableAssessmentStatuses.map {
        Arguments.of(
          Cas1Application(
            id = UUID.randomUUID(),
            applicationStatus = it,
            placementStatus = null
          )
        )
      }.stream()

    @JvmStatic
    fun provideUncompletedPlacementAllocatedCas1Applications() =
      uncompletedPlacementStatuses.map {
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
