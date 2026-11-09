package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.rules

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1ApplicationV2
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas1PendingPlacementRequestRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildCas1ApplicationDomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData

class Cas1PendingPlacementRequestRuleTest {
  private val description = "FAIL if referral is pending placement request"

  @Test
  fun `referral pending placement request fails`() {
    val data = buildDomainData(
      cas1Application = buildCas1ApplicationDomainData(
        application = buildCas1ApplicationV2(status = Cas1ApplicationStatus.PENDING_PLACEMENT_REQUEST),
        currentOutcome = null,
      ),
    )

    val result = Cas1PendingPlacementRequestRule().evaluate(data)

    assertThat(result).isEqualTo(RuleResult(description, RuleStatus.FAIL))
  }

  @ParameterizedTest
  @EnumSource(value = Cas1ApplicationStatus::class, names = ["PENDING_PLACEMENT_REQUEST"], mode = EnumSource.Mode.EXCLUDE)
  fun `referral not pending placement request passes`(status: Cas1ApplicationStatus) {
    val data = buildDomainData(
      cas1Application = buildCas1ApplicationDomainData(
        application = buildCas1ApplicationV2(status = status),
        currentOutcome = null,
      ),
    )

    val result = Cas1PendingPlacementRequestRule().evaluate(data)

    assertThat(result).isEqualTo(RuleResult(description, RuleStatus.PASS))
  }

  @Test
  fun `missing referral passes`() {
    val result = Cas1PendingPlacementRequestRule().evaluate(buildDomainData(cas1Application = null))

    assertThat(result).isEqualTo(RuleResult(description, RuleStatus.PASS))
  }
}
