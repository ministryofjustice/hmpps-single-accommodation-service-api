package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas3.rules

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.enums.Cas3ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.enums.Cas3PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas3Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.rules.Cas3ApplicationCompletionRule
import java.time.LocalDate

class Cas3ApplicationCompletionRuleTest {
  private val crn = "ABC234"
  private val male = SexCode.M
  private val tier = TierScore.A1
  private val description = "FAIL if CAS3 application is not complete"

  @ParameterizedTest
  @MethodSource("uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas3.rules.Cas3ApplicationCompletionRuleTest#provideCompletedCas3Applications")
  fun `application is completed so rule passes`(cas3Application: Cas3Application) {
    val data = DomainData(
      crn = crn,
      tier = tier,
      sex = male,
      releaseDate = LocalDate.now().plusMonths(5),
      cas3Application = cas3Application
    )

    val result = Cas3ApplicationCompletionRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.PASS,
      )
    )
  }

  @ParameterizedTest
  @MethodSource("uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas3.rules.Cas3ApplicationCompletionRuleTest#provideUncompletedPlacedCas3Applications")
  fun `application is PLACED but placement not completed so rule fails`(cas3Application: Cas3Application) {
    val data = DomainData(
      crn = crn,
      tier = tier,
      sex = male,
      releaseDate = LocalDate.now().plusMonths(5),
      cas3Application = cas3Application
    )

    val result = Cas3ApplicationCompletionRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.FAIL,
      )
    )
  }

  @ParameterizedTest
  @MethodSource("uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas3.rules.Cas3ApplicationCompletionRuleTest#provideNonPlacedCas3Applications")
  fun `application is not PLACED so rule fails`(cas3Application: Cas3Application) {
    val data = DomainData(
      crn = crn,
      tier = tier,
      sex = male,
      releaseDate = LocalDate.now().plusMonths(5),
      cas3Application = cas3Application
    )

    val result = Cas3ApplicationCompletionRule().evaluate(data)

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
      cas3Application = null
    )

    val result = Cas3ApplicationCompletionRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.FAIL,
      )
    )
  }

  @Test
  fun `rule has correct description`() {
    assertThat(Cas3ApplicationCompletionRule().description).isEqualTo(description)
  }

  private companion object {

    val completedPlacementStatuses = listOf(
      Cas3PlacementStatus.PROVISIONAL,
      Cas3PlacementStatus.CONFIRMED,
      Cas3PlacementStatus.ARRIVED,
    )

    val uncompletedPlacementStatuses = listOf(
      Cas3PlacementStatus.DEPARTED,
      Cas3PlacementStatus.NOT_ARRIVED,
      Cas3PlacementStatus.CANCELLED,
      Cas3PlacementStatus.CLOSED,
    )

    val nonPlacedStatuses = listOf(
      Cas3ApplicationStatus.IN_PROGRESS,
      Cas3ApplicationStatus.SUBMITTED,
      Cas3ApplicationStatus.REQUESTED_FURTHER_INFORMATION,
      Cas3ApplicationStatus.PENDING,
      Cas3ApplicationStatus.REJECTED,
      Cas3ApplicationStatus.AWAITING_PLACEMENT,
      Cas3ApplicationStatus.INAPPLICABLE,
      Cas3ApplicationStatus.WITHDRAWN,
    )

    @JvmStatic
    fun provideCompletedCas3Applications() =
      completedPlacementStatuses.map {
        Arguments.of(
          buildCas3Application(
            applicationStatus = Cas3ApplicationStatus.PLACED,
            placementStatus = it
          )
        )
      }.stream()

    @JvmStatic
    fun provideUncompletedPlacedCas3Applications() =
      uncompletedPlacementStatuses.map {
        Arguments.of(
          buildCas3Application(
            applicationStatus = Cas3ApplicationStatus.PLACED,
            placementStatus = it
          )
        )
      }.stream()

    @JvmStatic
    fun provideNonPlacedCas3Applications() =
      nonPlacedStatuses.map {
        Arguments.of(
          buildCas3Application(
            applicationStatus = it,
            placementStatus = null
          )
        )
      }.stream()
  }
}
