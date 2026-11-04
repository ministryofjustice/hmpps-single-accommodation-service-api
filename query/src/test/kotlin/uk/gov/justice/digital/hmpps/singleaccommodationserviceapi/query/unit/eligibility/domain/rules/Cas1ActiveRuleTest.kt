package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.rules

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1ApplicationV2
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas1ActiveRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildCas1ApplicationDomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import java.util.UUID

class Cas1ActiveRuleTest {
  private val description = "FAIL if CAS1 referral is submitted and active"

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
    val cas1Application = buildCas1ApplicationV2(status = status, id = UUID.randomUUID())

    val data = buildDomainData(
      cas1Application = buildCas1ApplicationDomainData(cas1Application, currentOutcome = null),
    )

    val result = Cas1ActiveRule().evaluate(data)

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
    val cas1Application = buildCas1ApplicationV2(status = status, id = UUID.randomUUID())

    val data = buildDomainData(
      cas1Application = buildCas1ApplicationDomainData(cas1Application, currentOutcome = null),
    )

    val result = Cas1ActiveRule().evaluate(data)

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

    val result = Cas1ActiveRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.PASS,
      ),
    )
  }
}
