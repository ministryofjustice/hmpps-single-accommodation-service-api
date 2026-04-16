package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.common

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.ReleaseDateValidationRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import java.time.LocalDate

class ReleaseDateValidationRuleTest {
  private val crn = "ABC234"

  @Test
  fun `candidate passes if release date is present`() {
    val data = buildDomainData(
      crn = crn,
      tierScore = TierScore.A1,
      sex = SexCode.M,
      releaseDate = LocalDate.now().plusYears(1),
    )

    val result = ReleaseDateValidationRule().evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.PASS)
  }

  @Test
  fun `candidate fails if release date is missing`() {
    val data = buildDomainData(
      crn = crn,
      tierScore = TierScore.A1,
      sex = SexCode.M,
      releaseDate = null,
    )

    val result = ReleaseDateValidationRule().evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.FAIL)
  }

  @Test
  fun `rule has correct description`() {
    val result = ReleaseDateValidationRule().description
    assertThat(result).isEqualTo("FAIL if candidate has no release date")
  }
}
