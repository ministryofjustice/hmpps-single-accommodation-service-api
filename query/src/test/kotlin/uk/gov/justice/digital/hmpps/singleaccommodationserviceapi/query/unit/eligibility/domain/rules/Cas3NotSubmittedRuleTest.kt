package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.rules

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas3Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas3NotSubmittedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData

class Cas3NotSubmittedRuleTest {
  private val rule = Cas3NotSubmittedRule()
  private val description = "FAIL if the CAS3 application is not submitted"

  @ParameterizedTest
  @EnumSource(value = Cas3ApplicationStatus::class, names = ["IN_PROGRESS"], mode = EnumSource.Mode.EXCLUDE)
  fun `submitted application passes`(status: Cas3ApplicationStatus) {
    val result = rule.evaluate(buildDomainData(cas3Application = buildCas3Application(applicationStatus = status)))

    assertThat(result).isEqualTo(RuleResult(description, RuleStatus.PASS))
  }

  @Test
  fun `in progress application fails`() {
    val result = rule.evaluate(
      buildDomainData(cas3Application = buildCas3Application(applicationStatus = Cas3ApplicationStatus.IN_PROGRESS)),
    )

    assertThat(result).isEqualTo(RuleResult(description, RuleStatus.FAIL))
  }

  @Test
  fun `missing application fails`() {
    val result = rule.evaluate(buildDomainData(cas3Application = null))

    assertThat(result).isEqualTo(RuleResult(description, RuleStatus.FAIL))
  }
}
