package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas1.requestForPlacement.rules

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1RequestForPlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1RequestForPlacementSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.requestForPlacement.rules.NoRequestForPlacementRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData

class NoRequestForPlacementRuleTest {
  private val rule = NoRequestForPlacementRule()
  private val description = "FAIL if no request for placement"

  @ParameterizedTest
  @EnumSource(Cas1RequestForPlacementStatus::class)
  fun `request has a status so rule passes`(status: Cas1RequestForPlacementStatus) {
    val data = buildDomainData(
      cas1Application = buildCas1Application(requestForPlacement = buildCas1RequestForPlacementSummary(status = status)),
    )

    assertThat(rule.evaluate(data)).isEqualTo(RuleResult(description, RuleStatus.PASS))
  }

  @Test
  fun `request has no status so rule fails`() {
    val data = buildDomainData(
      cas1Application = buildCas1Application(requestForPlacement = buildCas1RequestForPlacementSummary(status = null)),
    )

    assertThat(rule.evaluate(data)).isEqualTo(RuleResult(description, RuleStatus.FAIL))
  }

  @Test
  fun `request is missing so rule fails`() {
    val data = buildDomainData(cas1Application = buildCas1Application(requestForPlacement = null))

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
