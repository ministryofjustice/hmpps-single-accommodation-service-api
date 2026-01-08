package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas1.rules

import org.assertj.core.api.Assertions.assertThat
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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.rules.ApplicationCompletionRule
import java.time.LocalDate
import java.util.UUID

class ApplicationCompletionRuleTest {
  private val crn = "ABC234"
  private val male = buildSex(SexCode.M)
  private val tier = TierScore.A1
  private val description = "FAIL if application is not complete"

  @ParameterizedTest
  @MethodSource("uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas1.rules.ApplicationCompletionRuleTest#provideCompletedCas1Applications")
  fun `application is completed so rule passes`(cas1Application: Cas1Application) {
    val data = DomainData(
      crn = crn,
      tier = tier,
      sex = male,
      releaseDate = LocalDate.now().plusMonths(5),
      cas1Application = cas1Application
    )

    val result = ApplicationCompletionRule().evaluate(data)

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
  @MethodSource("uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas1.rules.ApplicationCompletionRuleTest#provideUncompletedPlacementAllocatedCas1Applications")
  fun `application is suitable (PLACEMENT_ALLOCATED) but not completed so rule fails`(cas1Application: Cas1Application) {
    val data = DomainData(
      crn = crn,
      tier = tier,
      sex = male,
      releaseDate = LocalDate.now().plusMonths(5),
      cas1Application = cas1Application
    )

    val result = ApplicationCompletionRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.FAIL,
        actionable = true,
        potentialAction = RuleAction("Create Placement"),
      )
    )
  }

  @ParameterizedTest
  @MethodSource("uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas1.rules.ApplicationCompletionRuleTest#provideSuitableCreatePlacementCas1Applications")
  fun `application is suitable (not PLACEMENT_ALLOCATED) but not completed and needs to create a placement so rule fails`(cas1Application: Cas1Application) {
    val data = DomainData(
      crn = crn,
      tier = tier,
      sex = male,
      releaseDate = LocalDate.now().plusMonths(5),
      cas1Application = cas1Application
    )

    val result = ApplicationCompletionRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.FAIL,
        actionable = true,
        potentialAction = RuleAction("Create Placement"),
      )
    )
  }

  @ParameterizedTest
  @MethodSource("uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas1.rules.ApplicationCompletionRuleTest#provideSuitableAssessmentCas1Applications")
  fun `application is suitable (not PLACEMENT_ALLOCATED) but not completed and needs to await assessment so rule fails`(cas1Application: Cas1Application) {
    val data = DomainData(
      crn = crn,
      tier = tier,
      sex = male,
      releaseDate = LocalDate.now().plusMonths(5),
      cas1Application = cas1Application
    )

    val result = ApplicationCompletionRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.FAIL,
        actionable = true,
        potentialAction = RuleAction("Await Assessment", true),
      )
    )
  }

  @Test
  fun `application is REQUEST_FOR_FURTHER_INFORMATION so rule fails`() {
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

    val result = ApplicationCompletionRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.FAIL,
        actionable = true,
        potentialAction = RuleAction("Provide Information"),
      )
    )
  }


  @ParameterizedTest
  @MethodSource("uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas1.rules.ApplicationCompletionRuleTest#provideUnsuitableCas1Applications")
  fun `application is unsuitable  so rule fails`(cas1Application: Cas1Application) {
    val data = DomainData(
      crn = crn,
      tier = tier,
      sex = male,
      releaseDate = LocalDate.now().plusMonths(5),
      cas1Application = cas1Application
    )

    val result = ApplicationCompletionRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.FAIL,
        actionable = true,
        potentialAction = null,
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

    val result = ApplicationCompletionRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.FAIL,
        actionable = true,
        potentialAction = null,
      )
    )
  }

  private companion object {

    val completedPlacementStatuses = listOf(
      Cas1PlacementStatus.UPCOMING,
      Cas1PlacementStatus.ARRIVED,
    )

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
    fun provideCompletedCas1Applications() =
      completedPlacementStatuses.map {
        Arguments.of(
          Cas1Application(
            id = UUID.randomUUID(),
            applicationStatus = Cas1ApplicationStatus.PLACEMENT_ALLOCATED,
            placementStatus = it
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




