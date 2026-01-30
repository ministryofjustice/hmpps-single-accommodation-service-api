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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.rules.Cas3ApplicationSuitabilityRule
import java.time.LocalDate

class Cas3ApplicationSuitabilityRuleTest {
  private val crn = "ABC234"
  private val male = SexCode.M
  private val tier = TierScore.A1
  private val description = "FAIL if candidate does not have a suitable application"

  @ParameterizedTest
  @MethodSource("uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas3.rules.Cas3ApplicationSuitabilityRuleTest#provideSuitableCas3Applications")
  fun `application is suitable (not PLACED) so rule passes`(cas3Application: Cas3Application) {
    val data = DomainData(
      crn = crn,
      tier = tier,
      sex = male,
      releaseDate = LocalDate.now().plusMonths(5),
      cas3Application = cas3Application
    )

    val result = Cas3ApplicationSuitabilityRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.PASS,
      )
    )
  }

  @ParameterizedTest
  @MethodSource("uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas3.rules.Cas3ApplicationSuitabilityRuleTest#providePlacedCas3Applications")
  fun `application is suitable (PLACED) so rule passes`(cas3Application: Cas3Application) {
    val data = DomainData(
      crn = crn,
      tier = tier,
      sex = male,
      releaseDate = LocalDate.now().plusMonths(5),
      cas3Application = cas3Application
    )

    val result = Cas3ApplicationSuitabilityRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.PASS,
      )
    )
  }

  @ParameterizedTest
  @MethodSource("uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas3.rules.Cas3ApplicationSuitabilityRuleTest#provideUnsuitableCas3Applications")
  fun `application does not have a suitable status so rule fails`(cas3Application: Cas3Application) {
    val data = DomainData(
      crn = crn,
      tier = tier,
      sex = male,
      releaseDate = LocalDate.now().plusMonths(5),
      cas3Application = cas3Application
    )

    val result = Cas3ApplicationSuitabilityRule().evaluate(data)

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

    val result = Cas3ApplicationSuitabilityRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.FAIL,
      )
    )
  }

  @Test
  fun `rule has correct description`() {
    assertThat(Cas3ApplicationSuitabilityRule().description).isEqualTo(description)
  }

  private companion object {

    val suitableStatuses = listOf(
      Cas3ApplicationStatus.IN_PROGRESS,
      Cas3ApplicationStatus.AWAITING_PLACEMENT,
      Cas3ApplicationStatus.PLACED,
      Cas3ApplicationStatus.REQUESTED_FURTHER_INFORMATION,
      Cas3ApplicationStatus.PENDING,
    )

    val unsuitableStatuses = listOf(
      Cas3ApplicationStatus.SUBMITTED,
      Cas3ApplicationStatus.REJECTED,
      Cas3ApplicationStatus.INAPPLICABLE,
      Cas3ApplicationStatus.WITHDRAWN,
    )

    @JvmStatic
    fun provideSuitableCas3Applications() =
      suitableStatuses.filter { it != Cas3ApplicationStatus.PLACED }.map {
        Arguments.of(
          buildCas3Application(
            applicationStatus = it,
            placementStatus = null
          )
        )
      }.stream()

    @JvmStatic
    fun providePlacedCas3Applications() =
      Cas3PlacementStatus.entries.map {
        Arguments.of(
          buildCas3Application(
            applicationStatus = Cas3ApplicationStatus.PLACED,
            placementStatus = it
          )
        )
      }.stream()

    @JvmStatic
    fun provideUnsuitableCas3Applications() =
      unsuitableStatuses.map {
        Arguments.of(
          buildCas3Application(
            applicationStatus = it,
            placementStatus = null
          )
        )
      }.stream()
  }
}
