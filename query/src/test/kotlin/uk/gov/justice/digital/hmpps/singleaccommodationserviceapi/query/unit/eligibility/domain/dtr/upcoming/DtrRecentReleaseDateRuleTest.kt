package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.dtr.upcoming

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationArrangementType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.RuleStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.upcoming.DtrRecentReleaseDateRule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.utils.MutableClock
import java.time.LocalDate

class DtrRecentReleaseDateRuleTest {
  private val clock = MutableClock()

  @Test
  fun `candidate passes when currentAccommodationArrangementType is missing`() {
    val data = buildDomainData(
      currentAccommodationArrangementType = null,
    )

    val result = DtrRecentReleaseDateRule(clock).evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.PASS)
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(value = AccommodationArrangementType::class, mode = EnumSource.Mode.EXCLUDE, names = ["PRISON"])
  fun `candidate passes when currentAccommodationArrangementType is not PRISON`(currentAccommodationArrangementType: AccommodationArrangementType) {
    val data = buildDomainData(
      currentAccommodationArrangementType = currentAccommodationArrangementType,
    )

    val result = DtrRecentReleaseDateRule(clock).evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.PASS)
  }

  @Test
  fun `candidate passes when currentAccommodationEndDate is present`() {
    val data = buildDomainData(
      currentAccommodationEndDate = LocalDate.now(clock),
    )

    val result = DtrRecentReleaseDateRule(clock).evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.PASS)
  }

  @Test
  fun `candidate passes when currentAccommodationArrangementType is PRISON and release date is missing`() {
    val data = buildDomainData(
      currentAccommodationArrangementType = AccommodationArrangementType.PRISON,
      releaseDate = null,
    )

    val result = DtrRecentReleaseDateRule(clock).evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.PASS)
  }

  @Test
  fun `candidate passes when currentAccommodationArrangementType is PRISON and release date is in the past`() {
    val data = buildDomainData(
      currentAccommodationArrangementType = AccommodationArrangementType.PRISON,
      releaseDate = LocalDate.now(clock).minusDays(1),
    )

    val result = DtrRecentReleaseDateRule(clock).evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.PASS)
  }

  @Test
  fun `candidate passes when currentAccommodationArrangementType is PRISON and release date is in less than 56 days`() {
    val data = buildDomainData(
      currentAccommodationArrangementType = AccommodationArrangementType.PRISON,
      releaseDate = LocalDate.now(clock).plusDays(10),
    )

    val result = DtrRecentReleaseDateRule(clock).evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.PASS)
  }

  @Test
  fun `candidate passes when currentAccommodationArrangementType is PRISON and release date is in exactly 56 days`() {
    val data = buildDomainData(
      currentAccommodationArrangementType = AccommodationArrangementType.PRISON,
      releaseDate = LocalDate.now(clock).plusDays(56),
    )

    val result = DtrRecentReleaseDateRule(clock).evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.PASS)
  }

  @Test
  fun `candidate fails when currentAccommodationArrangementType is PRISON and release date is in more than 56 days`() {
    val data = buildDomainData(
      currentAccommodationArrangementType = AccommodationArrangementType.PRISON,
      releaseDate = LocalDate.now(clock).plusDays(57),
    )

    val result = DtrRecentReleaseDateRule(clock).evaluate(data)

    assertThat(result.ruleStatus).isEqualTo(RuleStatus.FAIL)
  }

  @Test
  fun `rule has correct description`() {
    assertThat(DtrRecentReleaseDateRule(clock).description)
      .isEqualTo("FAIL if not within 56 days of release date from prison when current accommodation is prison")
  }
}
