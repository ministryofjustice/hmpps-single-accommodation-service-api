package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas1.rules

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.enums.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.enums.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.rules.ApplicationSuitabilityRule
import java.time.LocalDate
import java.util.UUID

class ApplicationSuitabilityRuleTest {
  private val crn = "ABC234"
  private val tier = TierScore.A1
  private val description = "FAIL if candidate does not have a suitable application"

  @ParameterizedTest(name = "{0}")
  @EnumSource(value = Cas1ApplicationStatus::class, names = [
    "AWAITING_ASSESSMENT",
    "UNALLOCATED_ASSESSMENT",
    "ASSESSMENT_IN_PROGRESS",
    "AWAITING_PLACEMENT",
    "REQUEST_FOR_FURTHER_INFORMATION",
    "PENDING_PLACEMENT_REQUEST",
  ])
  fun `application is suitable (but not PLACEMENT_ALLOCATED) so rule passes`(status: Cas1ApplicationStatus) {
    val cas1Application = Cas1Application(
      id = UUID.randomUUID(),
      applicationStatus = status,
      placementStatus = null
    )

    val data = DomainData(
      crn = crn,
      tier = tier,
      sex = SexCode.M,
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

  @ParameterizedTest(name = "{0}")
  @EnumSource(value = Cas1PlacementStatus::class)
  fun `application is suitable (PLACEMENT_ALLOCATED) so rule passes`(status: Cas1PlacementStatus) {
    val cas1Application = Cas1Application(
      id = UUID.randomUUID(),
      applicationStatus = Cas1ApplicationStatus.PLACEMENT_ALLOCATED,
      placementStatus = status
    )

    val data = DomainData(
      crn = crn,
      tier = tier,
      sex = SexCode.M,
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

  @ParameterizedTest(name = "{0}")
  @EnumSource(value = Cas1ApplicationStatus::class, names = [
    "EXPIRED",
    "INAPPLICABLE",
    "STARTED",
    "REJECTED",
    "WITHDRAWN",
  ])
  fun `application does not have a suitable status so rule fails`(status: Cas1ApplicationStatus) {
    val cas1Application = Cas1Application(
      id = UUID.randomUUID(),
      applicationStatus = status,
      placementStatus = null
    )

    val data = DomainData(
      crn = crn,
      tier = tier,
      sex = SexCode.M,
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
      sex = SexCode.M,
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
}




