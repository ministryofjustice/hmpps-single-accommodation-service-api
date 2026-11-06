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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.DepartedPlacementRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildCas1ApplicationDomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData

class DepartedPlacementRuleTest {
  private val description = "FAIL if current placement is departed"

  @Test
  fun `departed placement fails`() {
    val data = buildDomainData(
      cas1Application = buildCas1ApplicationDomainData(
        application = buildCas1ApplicationV2(status = Cas1ApplicationStatus.PLACEMENT_ALLOCATED),
        currentOutcome = buildCas1ExternalRequestForPlacementOutcomeDto(
          placement = buildCas1PlacementV2(status = Cas1PlacementStatus.DEPARTED),
        ),
      ),
    )

    val result = DepartedPlacementRule().evaluate(data)

    assertThat(result).isEqualTo(RuleResult(description, RuleStatus.FAIL))
  }

  @ParameterizedTest
  @EnumSource(value = Cas1PlacementStatus::class, names = ["DEPARTED"], mode = EnumSource.Mode.EXCLUDE)
  fun `placement that is not departed passes`(status: Cas1PlacementStatus) {
    val data = buildDomainData(
      cas1Application = buildCas1ApplicationDomainData(
        application = buildCas1ApplicationV2(status = Cas1ApplicationStatus.PLACEMENT_ALLOCATED),
        currentOutcome = buildCas1ExternalRequestForPlacementOutcomeDto(
          placement = buildCas1PlacementV2(status = status),
        ),
      ),
    )

    val result = DepartedPlacementRule().evaluate(data)

    assertThat(result).isEqualTo(RuleResult(description, RuleStatus.PASS))
  }

  @Test
  fun `missing placement passes`() {
    val data = buildDomainData(
      cas1Application = buildCas1ApplicationDomainData(
        application = buildCas1ApplicationV2(status = Cas1ApplicationStatus.PLACEMENT_ALLOCATED),
        currentOutcome = buildCas1ExternalRequestForPlacementOutcomeDto(
          placement = null,
        ),
      ),
    )

    val result = DepartedPlacementRule().evaluate(data)

    assertThat(result).isEqualTo(RuleResult(description, RuleStatus.PASS))
  }

  @Test
  fun `missing referral passes`() {
    val result = DepartedPlacementRule().evaluate(buildDomainData(cas1Application = null))

    assertThat(result).isEqualTo(RuleResult(description, RuleStatus.PASS))
  }
}
