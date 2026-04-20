package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.common

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.common.Cas3RecentCurrentAccommodationEndDateRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildCurrentAccommodation
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.utils.MutableClock
import java.time.LocalDate

class Cas3RecentCurrentAccommodationEndDateRuleTest {
  private val description = "FAIL if CAS3 application is not within 1 year of current accommodation end date"
  private val clock = MutableClock()

  @Test
  fun `current accommodation end date is within 1 year so rule passes`() {
    val currentAccommodationEndDate = LocalDate.now()
    val data = buildDomainData(
      currentAccommodation = buildCurrentAccommodation(currentAccommodationEndDate),
    )

    clock.setNow(currentAccommodationEndDate.minusMonths(1))

    val result = Cas3RecentCurrentAccommodationEndDateRule(clock).evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.PASS,
      ),
    )
  }

  @Test
  fun `current accommodation end date is exactly a year from now so rule passes`() {
    val currentAccommodationEndDate = LocalDate.now()
    val data = buildDomainData(
      currentAccommodation = buildCurrentAccommodation(currentAccommodationEndDate),
    )

    clock.setNow(currentAccommodationEndDate.minusYears(1))

    val result = Cas3RecentCurrentAccommodationEndDateRule(clock).evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.PASS,
      ),
    )
  }

  @Test
  fun `current accommodation end date is more than a year from now so rule fails`() {
    val currentAccommodationEndDate = LocalDate.now()
    val data = buildDomainData(
      currentAccommodation = buildCurrentAccommodation(currentAccommodationEndDate),
    )

    clock.setNow(currentAccommodationEndDate.minusYears(2))

    val result = Cas3RecentCurrentAccommodationEndDateRule(clock).evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.FAIL,
      ),
    )
  }

  @Test
  fun `current accommodation end date is in the past so rule passes`() {
    val currentAccommodationEndDate = LocalDate.now()
    val data = buildDomainData(
      currentAccommodation = buildCurrentAccommodation(currentAccommodationEndDate),
    )

    clock.setNow(currentAccommodationEndDate.plusDays(2))

    val result = Cas3RecentCurrentAccommodationEndDateRule(clock).evaluate(data)

    assertThat(result).isEqualTo(
      RuleResult(
        description = description,
        ruleStatus = RuleStatus.PASS,
      ),
    )
  }

  @Test
  fun `rule has correct description`() {
    assertThat(Cas3RecentCurrentAccommodationEndDateRule(clock).description).isEqualTo(description)
  }
}
