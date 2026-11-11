package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.rules

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1RequestForPlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1ExternalRequestForPlacementOutcomeDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1RequestForPlacementSummaryV2
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas1WithdrawnRequestRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildCas1ApplicationDomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData

class Cas1WithdrawnRequestRuleTest {
  private val description = "FAIL if request for placement is withdrawn"

  @Test
  fun `withdrawn request for placement fails`() {
    val data = buildDomainData(
      cas1Application = buildCas1ApplicationDomainData(
        currentOutcome = buildCas1ExternalRequestForPlacementOutcomeDto(
          requestForPlacement = buildCas1RequestForPlacementSummaryV2(status = Cas1RequestForPlacementStatus.REQUEST_WITHDRAWN),
        ),
      ),
    )

    val result = Cas1WithdrawnRequestRule().evaluate(data)

    assertThat(result).isEqualTo(RuleResult(description, RuleStatus.FAIL))
  }

  @ParameterizedTest
  @EnumSource(value = Cas1RequestForPlacementStatus::class, names = ["REQUEST_WITHDRAWN"], mode = EnumSource.Mode.EXCLUDE)
  fun `request for placement that is not withdrawn passes`(status: Cas1RequestForPlacementStatus) {
    val data = buildDomainData(
      cas1Application = buildCas1ApplicationDomainData(
        currentOutcome = buildCas1ExternalRequestForPlacementOutcomeDto(
          requestForPlacement = buildCas1RequestForPlacementSummaryV2(status = status),
        ),
      ),
    )

    val result = Cas1WithdrawnRequestRule().evaluate(data)

    assertThat(result).isEqualTo(RuleResult(description, RuleStatus.PASS))
  }

  @Test
  fun `missing current outcome passes`() {
    val data = buildDomainData(cas1Application = buildCas1ApplicationDomainData(currentOutcome = null))

    val result = Cas1WithdrawnRequestRule().evaluate(data)

    assertThat(result).isEqualTo(RuleResult(description, RuleStatus.PASS))
  }

  @Test
  fun `missing referral passes`() {
    val result = Cas1WithdrawnRequestRule().evaluate(buildDomainData(cas1Application = null))

    assertThat(result).isEqualTo(RuleResult(description, RuleStatus.PASS))
  }
}
