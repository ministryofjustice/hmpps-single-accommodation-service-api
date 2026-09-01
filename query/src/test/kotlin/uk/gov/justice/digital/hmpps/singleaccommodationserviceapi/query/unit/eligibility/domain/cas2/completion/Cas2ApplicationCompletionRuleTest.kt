package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas2.completion

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas2Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas2ApplicationSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.completion.Cas2ApplicationCompletionRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import java.util.UUID

class Cas2ApplicationCompletionRuleTest {
  private val description = "FAIL if application is not complete"

  @Test
  fun `application is complete so rule passes`() {
    val cas2Application = buildCas2Application(
      application = buildCas2ApplicationSummary(
        id = UUID.randomUUID(),
        status = "COMPLETED",
      ),
    )

    val data = buildDomainData(
      cas2Application = cas2Application,
    )

    val result = Cas2ApplicationCompletionRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.PASS,
      ),
    )
  }

  @Test
  fun `application not completed so rule fails`() {
    val cas2Application = buildCas2Application(
      application = buildCas2ApplicationSummary(
        status = "STARTED",
        id = UUID.randomUUID(),
      ),
    )

    val data = buildDomainData(
      cas2Application = cas2Application,
    )

    val result = Cas2ApplicationCompletionRule().evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.FAIL,
      ),
    )
  }
}
