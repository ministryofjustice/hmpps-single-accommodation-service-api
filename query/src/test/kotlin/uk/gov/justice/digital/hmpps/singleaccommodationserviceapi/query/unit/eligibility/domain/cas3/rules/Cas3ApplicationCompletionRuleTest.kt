package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas3.rules

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3PlacementStatus
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

  @ParameterizedTest(name = "{0}")
  @EnumSource(value = Cas3PlacementStatus::class, names = ["PROVISIONAL", "CONFIRMED", "ARRIVED"])
  fun `application is completed so rule passes`(placementStatus: Cas3PlacementStatus) {
    val data = DomainData(
      crn = crn,
      tier = tier,
      sex = male,
      releaseDate = LocalDate.now().plusMonths(5),
      cas3Application = buildCas3Application(
        applicationStatus = Cas3ApplicationStatus.PLACED,
        placementStatus = placementStatus,
      ),
    )

    val result = Cas3ApplicationCompletionRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.PASS,
      ),
    )
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(value = Cas3PlacementStatus::class, names = ["DEPARTED", "NOT_ARRIVED", "CANCELLED", "CLOSED"])
  fun `application is PLACED but placement not completed so rule fails`(placementStatus: Cas3PlacementStatus) {
    val data = DomainData(
      crn = crn,
      tier = tier,
      sex = male,
      releaseDate = LocalDate.now().plusMonths(5),
      cas3Application = buildCas3Application(
        applicationStatus = Cas3ApplicationStatus.PLACED,
        placementStatus = placementStatus,
      ),
    )

    val result = Cas3ApplicationCompletionRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.FAIL,
      ),
    )
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(value = Cas3ApplicationStatus::class, mode = EnumSource.Mode.EXCLUDE, names = ["PLACED"])
  fun `application is not PLACED so rule fails`(applicationStatus: Cas3ApplicationStatus) {
    val data = DomainData(
      crn = crn,
      tier = tier,
      sex = male,
      releaseDate = LocalDate.now().plusMonths(5),
      cas3Application = buildCas3Application(
        applicationStatus = applicationStatus,
      ),
    )

    val result = Cas3ApplicationCompletionRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.FAIL,
      ),
    )
  }

  @Test
  fun `application is not present so rule fails`() {
    val data = DomainData(
      crn = crn,
      tier = tier,
      sex = male,
      releaseDate = LocalDate.now().plusMonths(5),
      cas3Application = null,
    )

    val result = Cas3ApplicationCompletionRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.FAIL,
      ),
    )
  }

  @Test
  fun `rule has correct description`() {
    assertThat(Cas3ApplicationCompletionRule().description).isEqualTo(description)
  }
}
