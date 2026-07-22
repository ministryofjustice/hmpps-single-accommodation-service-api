package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.pa.eligibility

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.pa.eligibility.Cas1ApplicationNotSuitableRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import java.util.UUID

class Cas1ApplicationNotSuitableRuleTest {
  private val description = "FAIL if candidate has a suitable CAS1 application"

  @ParameterizedTest(name = "{0}")
  @EnumSource(
    value = Cas1ApplicationStatus::class,
    names = [
      "AWAITING_ASSESSMENT",
      "UNALLOCATED_ASSESSMENT",
      "ASSESSMENT_IN_PROGRESS",
      "AWAITING_PLACEMENT",
      "REQUESTED_FURTHER_INFORMATION",
      "PENDING_PLACEMENT_REQUEST",
      "PLACEMENT_ALLOCATED",
    ],
  )
  fun `application is suitable so rule fails`(status: Cas1ApplicationStatus) {
    val cas1Application = buildCas1Application(
      id = UUID.randomUUID(),
      applicationStatus = status,
      placementStatus = null,
      requestForPlacementStatus = null,
    )

    val data = buildDomainData(
      cas1Application = cas1Application,
    )

    val result = Cas1ApplicationNotSuitableRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.FAIL,
        failureReason = FailureReason.SUITABLE_CAS1_APPLICATION,
      ),
    )
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(
    value = Cas1ApplicationStatus::class,
    names = [
      "EXPIRED",
      "INAPPLICABLE",
      "STARTED",
      "REJECTED",
      "WITHDRAWN",
    ],
  )
  fun `application does not have a suitable status so rule passes`(status: Cas1ApplicationStatus) {
    val cas1Application = buildCas1Application(
      id = UUID.randomUUID(),
      applicationStatus = status,
      placementStatus = null,
      requestForPlacementStatus = null,
    )

    val data = buildDomainData(
      cas1Application = cas1Application,
    )

    val result = Cas1ApplicationNotSuitableRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.PASS,
      ),
    )
  }

  @Test
  fun `application is not present so rule passes`() {
    val data = buildDomainData(
      cas1Application = null,
    )

    val result = Cas1ApplicationNotSuitableRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.PASS,
      ),
    )
  }
}
