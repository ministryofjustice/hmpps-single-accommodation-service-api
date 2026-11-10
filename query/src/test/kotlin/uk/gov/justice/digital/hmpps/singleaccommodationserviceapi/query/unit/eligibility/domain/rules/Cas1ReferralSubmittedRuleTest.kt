package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.rules

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1ApplicationV2
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.rules.Cas1ReferralSubmittedRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildCas1ApplicationDomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData

class Cas1ReferralSubmittedRuleTest {
  private val description = "FAIL if the referral is submitted"

  @ParameterizedTest
  @EnumSource(value = Cas1ApplicationStatus::class, names = ["STARTED"], mode = EnumSource.Mode.EXCLUDE)
  fun `submitted referral fails`(status: Cas1ApplicationStatus) {
    val data = buildDomainData(
      cas1Application = buildCas1ApplicationDomainData(
        buildCas1ApplicationV2(
          status = status,
        ),
        currentOutcome = null,
      ),
    )

    val result = Cas1ReferralSubmittedRule().evaluate(data)

    assertThat(result).isEqualTo(RuleResult(description, RuleStatus.FAIL))
  }

  @Test
  fun `unsubmitted referral passes`() {
    val data = buildDomainData(
      cas1Application = buildCas1ApplicationDomainData(
        buildCas1ApplicationV2(
          status = Cas1ApplicationStatus.STARTED,
        ),
        currentOutcome = null,
      ),
    )
    val result = Cas1ReferralSubmittedRule().evaluate(data)

    assertThat(result).isEqualTo(RuleResult(description, RuleStatus.PASS))
  }

  @Test
  fun `missing referral passes`() {
    val result = Cas1ReferralSubmittedRule().evaluate(buildDomainData(cas1Application = null))

    assertThat(result).isEqualTo(RuleResult(description, RuleStatus.PASS))
  }
}
