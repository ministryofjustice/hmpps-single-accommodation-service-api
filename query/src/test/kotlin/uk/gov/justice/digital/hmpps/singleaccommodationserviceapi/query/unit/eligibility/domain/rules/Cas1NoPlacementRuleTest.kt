package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.rules

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1ApplicationV2
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1ExternalRequestForPlacementOutcomeDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1PlacementV2
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas1NoPlacementRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildCas1ApplicationDomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData

class Cas1NoPlacementRuleTest {
  private val description = "FAIL if no placement"

  @ParameterizedTest
  @EnumSource(Cas1PlacementStatus::class)
  fun `placement with status passes`(status: Cas1PlacementStatus) {
    val data = buildDomainData(
      cas1Application = buildCas1ApplicationDomainData(
        application = buildCas1ApplicationV2(status = Cas1ApplicationStatus.PLACEMENT_ALLOCATED),
        currentOutcome = buildCas1ExternalRequestForPlacementOutcomeDto(
          placement = buildCas1PlacementV2(status),
        ),
      ),
    )

    val result = Cas1NoPlacementRule().evaluate(data)

    assertThat(result).isEqualTo(RuleResult(description, RuleStatus.PASS))
  }

  @Test
  fun `missing placement fails`() {
    val data = buildDomainData(
      cas1Application = buildCas1ApplicationDomainData(
        application = buildCas1ApplicationV2(status = Cas1ApplicationStatus.PLACEMENT_ALLOCATED),
        currentOutcome = buildCas1ExternalRequestForPlacementOutcomeDto(
          placement = null,
        ),
      ),
    )

    val result = Cas1NoPlacementRule().evaluate(data)

    assertThat(result).isEqualTo(RuleResult(description, RuleStatus.FAIL))
  }

  @Test
  fun `missing referral fails`() {
    val result = Cas1NoPlacementRule().evaluate(buildDomainData(cas1Application = null))

    assertThat(result).isEqualTo(RuleResult(description, RuleStatus.FAIL))
  }
}
