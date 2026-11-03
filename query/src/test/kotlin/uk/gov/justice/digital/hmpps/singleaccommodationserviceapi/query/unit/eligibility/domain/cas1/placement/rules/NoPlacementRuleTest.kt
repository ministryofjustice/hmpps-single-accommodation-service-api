package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas1.placement.rules

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1PlacementSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.placement.rules.NoPlacementRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData

class NoPlacementRuleTest {
  private val rule = NoPlacementRule()
  private val description = "FAIL if no placement"

  @ParameterizedTest
  @EnumSource(Cas1PlacementStatus::class)
  fun `placement has a status so rule passes`(status: Cas1PlacementStatus) {
    val data = buildDomainData(
      cas1Application = buildCas1Application(placement = buildCas1PlacementSummary(status = status)),
    )

    assertThat(rule.evaluate(data)).isEqualTo(RuleResult(description, RuleStatus.PASS))
  }

  @Test
  fun `placement has no status so rule fails`() {
    val data = buildDomainData(
      cas1Application = buildCas1Application(placement = buildCas1PlacementSummary(status = null)),
    )

    assertThat(rule.evaluate(data)).isEqualTo(RuleResult(description, RuleStatus.FAIL))
  }

  @Test
  fun `placement is missing so rule fails`() {
    val data = buildDomainData(cas1Application = buildCas1Application(placement = null))

    assertThat(rule.evaluate(data)).isEqualTo(RuleResult(description, RuleStatus.FAIL))
  }

  @Test
  fun `application is missing so rule fails`() {
    assertThat(rule.evaluate(buildDomainData(cas1Application = null)))
      .isEqualTo(RuleResult(description, RuleStatus.FAIL))
  }

  @Test
  fun `rule has correct description`() {
    assertThat(rule.description).isEqualTo(description)
  }
}
