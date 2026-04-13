package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas1.rules.validation

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.rules.validation.Cas1ReleaseDateValidationRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import java.time.LocalDate

class Cas1ReleaseDateValidationRuleTest {
  private val crn = "ABC234"

  @Test
  fun `candidate passes if release date is present`() {
    val data = buildDomainData(
      crn = crn,
      tierScore = TierScore.A1,
      sex = SexCode.M,
      releaseDate = LocalDate.now().plusYears(1),
    )

    val result = Cas1ReleaseDateValidationRule().evaluate(data)

    Assertions.assertThat(result.ruleStatus).isEqualTo(RuleStatus.PASS)
  }

  @Test
  fun `candidate fails if release date is missing`() {
    val data = buildDomainData(
      crn = crn,
      tierScore = TierScore.A1,
      sex = SexCode.M,
      releaseDate = null,
    )

    val result = Cas1ReleaseDateValidationRule().evaluate(data)

    Assertions.assertThat(result.ruleStatus).isEqualTo(RuleStatus.FAIL)
  }

  @Test
  fun `rule has correct description`() {
    val result = Cas1ReleaseDateValidationRule().description
    Assertions.assertThat(result).isEqualTo("FAIL if candidate has no release date")
  }
}
