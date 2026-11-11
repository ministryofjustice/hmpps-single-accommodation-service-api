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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.PlacementNotActiveRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildCas1ApplicationDomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData

class PlacementNotActiveRuleTest {
  private val description = "FAIL if current placement is not active"

  @ParameterizedTest
  @EnumSource(value = Cas1PlacementStatus::class, names = ["ARRIVED", "UPCOMING"])
  fun `active placement passes`(status: Cas1PlacementStatus) {
    val data = buildDomainData(
      cas1Application = buildCas1ApplicationDomainData(
        application = buildCas1ApplicationV2(status = Cas1ApplicationStatus.PLACEMENT_ALLOCATED),
        currentOutcome = buildCas1ExternalRequestForPlacementOutcomeDto(
          placement = buildCas1PlacementV2(status = status),
        ),
      ),
    )

    val result = PlacementNotActiveRule().evaluate(data)

    assertThat(result).isEqualTo(RuleResult(description, RuleStatus.PASS))
  }

  @ParameterizedTest
  @EnumSource(value = Cas1PlacementStatus::class, names = ["ARRIVED", "UPCOMING"], mode = EnumSource.Mode.EXCLUDE)
  fun `inactive placement fails`(status: Cas1PlacementStatus) {
    val data = buildDomainData(
      cas1Application = buildCas1ApplicationDomainData(
        application = buildCas1ApplicationV2(status = Cas1ApplicationStatus.PLACEMENT_ALLOCATED),
        currentOutcome = buildCas1ExternalRequestForPlacementOutcomeDto(
          placement = buildCas1PlacementV2(status = status),
        ),
      ),
    )

    val result = PlacementNotActiveRule().evaluate(data)

    assertThat(result).isEqualTo(RuleResult(description, RuleStatus.FAIL))
  }

  @Test
  fun `missing placement fails`() {
    val data = buildDomainData(
      cas1Application = buildCas1ApplicationDomainData(
        currentOutcome = buildCas1ExternalRequestForPlacementOutcomeDto(placement = null),
      ),
    )

    val result = PlacementNotActiveRule().evaluate(data)

    assertThat(result).isEqualTo(RuleResult(description, RuleStatus.FAIL))
  }

  @Test
  fun `missing current outcome fails`() {
    val data = buildDomainData(cas1Application = buildCas1ApplicationDomainData(currentOutcome = null))

    val result = PlacementNotActiveRule().evaluate(data)

    assertThat(result).isEqualTo(RuleResult(description, RuleStatus.FAIL))
  }

  @Test
  fun `missing referral fails`() {
    val result = PlacementNotActiveRule().evaluate(buildDomainData(cas1Application = null))

    assertThat(result).isEqualTo(RuleResult(description, RuleStatus.FAIL))
  }
}
