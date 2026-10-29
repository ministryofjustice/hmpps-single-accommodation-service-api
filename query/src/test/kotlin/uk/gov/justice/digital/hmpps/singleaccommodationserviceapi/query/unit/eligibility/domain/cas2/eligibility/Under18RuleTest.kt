package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas2.eligibility

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.eligibility.Under18Rule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.utils.MutableClock
import java.time.LocalDate

class Under18RuleTest {
  private val description = "FAIL if individual is under 18 years old"
  private val clock = MutableClock()

  @Test
  fun `individual is under 18 years old so rule fails`() {
    val data = buildDomainData(
      dob = LocalDate.now(clock).minusYears(18).plusDays(1),
    )

    val result = Under18Rule(clock).evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.FAIL,
        failureReason = FailureReason.UNDER_18,
      ),
    )
  }

  @Test
  fun `individual is 18 years old so rule passes`() {
    val data = buildDomainData(
      dob = LocalDate.now(clock).minusYears(18),
    )

    val result = Under18Rule(clock).evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.PASS,
      ),
    )
  }

  @Test
  fun `dob is missing so rule passes`() {
    val data = buildDomainData(
      dob = null,
    )

    val result = Under18Rule(clock).evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.PASS,
      ),
    )
  }
}
