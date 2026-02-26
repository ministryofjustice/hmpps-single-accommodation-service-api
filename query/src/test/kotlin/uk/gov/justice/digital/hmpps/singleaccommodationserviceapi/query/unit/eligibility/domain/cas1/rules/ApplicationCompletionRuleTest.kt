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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.rules.ApplicationCompletionRule
import java.time.LocalDate
import java.util.UUID

class ApplicationCompletionRuleTest {
  private val crn = "ABC234"
  private val tier = TierScore.A1
  private val description = "FAIL if application is not complete"

  @ParameterizedTest(name = "{0}")
  @EnumSource(value = Cas1PlacementStatus::class, names = ["UPCOMING", "ARRIVED"])
  fun `application is completed so rule passes - status = `(status: Cas1PlacementStatus) {
    val cas1Application = Cas1Application(
      id = UUID.randomUUID(),
      applicationStatus = Cas1ApplicationStatus.PLACEMENT_ALLOCATED,
      placementStatus = status,
    )

    val data = DomainData(
      crn = crn,
      tier = tier,
      sex = SexCode.M,
      releaseDate = LocalDate.now().plusMonths(5),
      cas1Application = cas1Application,
    )

    val result = ApplicationCompletionRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.PASS,
      ),
    )
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(value = Cas1PlacementStatus::class, names = ["DEPARTED", "CANCELLED", "NOT_ARRIVED"])
  fun `application is suitable (PLACEMENT_ALLOCATED) but not completed so rule fails - status = `(status: Cas1PlacementStatus) {
    val cas1Application = Cas1Application(
      id = UUID.randomUUID(),
      applicationStatus = Cas1ApplicationStatus.PLACEMENT_ALLOCATED,
      placementStatus = status,
    )

    val data = DomainData(
      crn = crn,
      tier = tier,
      sex = SexCode.M,
      releaseDate = LocalDate.now().plusMonths(5),
      cas1Application = cas1Application,
    )

    val result = ApplicationCompletionRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.FAIL,
      ),
    )
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(value = Cas1ApplicationStatus::class, names = ["AWAITING_PLACEMENT", "PENDING_PLACEMENT_REQUEST"])
  fun `application is suitable (not PLACEMENT_ALLOCATED) but not completed and needs to create a placement so rule fails`(status: Cas1ApplicationStatus) {
    val cas1Application = Cas1Application(
      id = UUID.randomUUID(),
      applicationStatus = status,
      placementStatus = null,
    )

    val data = DomainData(
      crn = crn,
      tier = tier,
      sex = SexCode.M,
      releaseDate = LocalDate.now().plusMonths(5),
      cas1Application = cas1Application,
    )

    val result = ApplicationCompletionRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.FAIL,
      ),
    )
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(value = Cas1ApplicationStatus::class, names = ["AWAITING_ASSESSMENT", "UNALLOCATED_ASSESSMENT", "ASSESSMENT_IN_PROGRESS"])
  fun `application is suitable (not PLACEMENT_ALLOCATED) but not completed and needs to await assessment so rule fails`(status: Cas1ApplicationStatus) {
    val cas1Application = Cas1Application(
      id = UUID.randomUUID(),
      applicationStatus = status,
      placementStatus = null,
    )

    val data = DomainData(
      crn = crn,
      tier = tier,
      sex = SexCode.M,
      releaseDate = LocalDate.now().plusMonths(5),
      cas1Application = cas1Application,
    )

    val result = ApplicationCompletionRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.FAIL,
      ),
    )
  }

  @Test
  fun `application is REQUEST_FOR_FURTHER_INFORMATION so rule fails`() {
    val data = DomainData(
      crn = crn,
      tier = tier,
      sex = SexCode.M,
      releaseDate = LocalDate.now().plusMonths(5),
      cas1Application = Cas1Application(
        id = UUID.randomUUID(),
        applicationStatus = Cas1ApplicationStatus.REQUEST_FOR_FURTHER_INFORMATION,
        placementStatus = null,
      ),
    )

    val result = ApplicationCompletionRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.FAIL,
      ),
    )
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(value = Cas1ApplicationStatus::class, names = ["EXPIRED", "INAPPLICABLE", "STARTED", "REJECTED", "WITHDRAWN"])
  fun `application is unsuitable  so rule fails`(status: Cas1ApplicationStatus) {
    val cas1Application = Cas1Application(
      id = UUID.randomUUID(),
      applicationStatus = status,
      placementStatus = null,
    )

    val data = DomainData(
      crn = crn,
      tier = tier,
      sex = SexCode.M,
      releaseDate = LocalDate.now().plusMonths(5),
      cas1Application = cas1Application,
    )

    val result = ApplicationCompletionRule().evaluate(data)

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
      sex = SexCode.M,
      releaseDate = LocalDate.now().plusMonths(5),
      cas1Application = null,
    )

    val result = ApplicationCompletionRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.FAIL,
      ),
    )
  }
}
