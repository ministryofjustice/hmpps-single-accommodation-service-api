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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.rules.Cas3ApplicationSuitabilityRule
import java.time.LocalDate

class Cas3ApplicationSuitabilityRuleTest {
  private val crn = "ABC234"
  private val male = SexCode.M
  private val tier = TierScore.A1
  private val description = "FAIL if candidate does not have a suitable application"

  @ParameterizedTest(name = "{0}")
  @EnumSource(value = Cas3ApplicationStatus::class, names = ["IN_PROGRESS", "AWAITING_PLACEMENT", "REQUESTED_FURTHER_INFORMATION", "PENDING"])
  fun `application is suitable (not PLACED) so rule passes`(applicationStatus: Cas3ApplicationStatus) {
    val data = DomainData(
      crn = crn,
      tier = tier,
      sex = male,
      releaseDate = LocalDate.now().plusMonths(5),
      cas3Application = buildCas3Application(
        applicationStatus = applicationStatus,
      ),
    )

    val result = Cas3ApplicationSuitabilityRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.PASS,
      ),
    )
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(Cas3PlacementStatus::class)
  fun `application is suitable (PLACED) so rule passes`(placementStatus: Cas3PlacementStatus) {
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

    val result = Cas3ApplicationSuitabilityRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.PASS,
      ),
    )
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(value = Cas3ApplicationStatus::class, names = ["SUBMITTED", "REJECTED", "INAPPLICABLE", "WITHDRAWN"])
  fun `application does not have a suitable status so rule fails`(applicationStatus: Cas3ApplicationStatus) {
    val data = DomainData(
      crn = crn,
      tier = tier,
      sex = male,
      releaseDate = LocalDate.now().plusMonths(5),
      cas3Application = buildCas3Application(
        applicationStatus = applicationStatus,
      ),
    )

    val result = Cas3ApplicationSuitabilityRule().evaluate(data)

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

    val result = Cas3ApplicationSuitabilityRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.FAIL,
      ),
    )
  }

  @Test
  fun `rule has correct description`() {
    assertThat(Cas3ApplicationSuitabilityRule().description).isEqualTo(description)
  }
}
