package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.rules

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1RequestForPlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1RequestForPlacementSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.NoRequestForPlacementRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData

class NoRequestForPlacementRuleTest {
  private val description = "FAIL if no current request for placement"

  @ParameterizedTest
  @EnumSource(Cas1RequestForPlacementStatus::class)
  fun `request for placement with status passes`(status: Cas1RequestForPlacementStatus) {
    val data = buildDomainData(
      cas1Application = buildCas1Application(
        requestForPlacement = buildCas1RequestForPlacementSummary(status = status),
      ),
    )

    val result = NoRequestForPlacementRule().evaluate(data)

    assertThat(result).isEqualTo(RuleResult(description, RuleStatus.PASS))
  }

  @Test
  fun `request for placement with missing status fails`() {
    val data = buildDomainData(
      cas1Application = buildCas1Application(
        requestForPlacement = buildCas1RequestForPlacementSummary(status = null),
      ),
    )

    val result = NoRequestForPlacementRule().evaluate(data)

    assertThat(result).isEqualTo(RuleResult(description, RuleStatus.FAIL))
  }

  @Test
  fun `missing request for placement fails`() {
    val data = buildDomainData(cas1Application = buildCas1Application(requestForPlacement = null))

    val result = NoRequestForPlacementRule().evaluate(data)

    assertThat(result).isEqualTo(RuleResult(description, RuleStatus.FAIL))
  }

  @Test
  fun `missing referral fails`() {
    val result = NoRequestForPlacementRule().evaluate(buildDomainData(cas1Application = null))

    assertThat(result).isEqualTo(RuleResult(description, RuleStatus.FAIL))
  }
}
