package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas1.completion

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.completion.Cas1ApplicationCompletionRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import java.util.UUID

class Cas1ApplicationCompletionRuleTest {
  private val description = "FAIL if placement is not upcoming"

  @Test
  fun `placement is upcoming so rule passes`() {
    val cas1Application = buildCas1Application(
      id = UUID.randomUUID(),
      placementStatus = Cas1PlacementStatus.UPCOMING,
    )

    val data = buildDomainData(
      cas1Application = cas1Application,
    )

    val result = Cas1ApplicationCompletionRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.PASS,
      ),
    )
  }

  @Test
  fun `placement is null so rule fails`() {
    val cas1Application = buildCas1Application(
      id = UUID.randomUUID(),
      placementStatus = null,
    )

    val data = buildDomainData(
      cas1Application = cas1Application,
    )

    val result = Cas1ApplicationCompletionRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.FAIL,
      ),
    )
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(value = Cas1PlacementStatus::class, names = ["DEPARTED", "CANCELLED", "NOT_ARRIVED", "ARRIVED"])
  fun `application not upcoming so rule fails - status = `(status: Cas1PlacementStatus) {
    val cas1Application = buildCas1Application(
      id = UUID.randomUUID(),
      placementStatus = status,
    )

    val data = buildDomainData(
      cas1Application = cas1Application,
    )

    val result = Cas1ApplicationCompletionRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.FAIL,
      ),
    )
  }
}
