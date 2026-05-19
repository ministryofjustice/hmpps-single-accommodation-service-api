package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.crs.upcoming

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.upcoming.CrsUpcomingContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.utils.MutableClock
import java.time.LocalDate

class CrsUpcomingContextUpdaterTest {
  private val clock = MutableClock()
  private val updater = CrsUpcomingContextUpdater(clock)

  @Nested
  inner class UpdateTests {
    @Test
    fun `update returns UPCOMING with accommodation referral action for male`() {
      val today = LocalDate.of(2025, 1, 1)
      clock.setNow(today)
      val data = buildDomainData(
        sex = SexCode.M,
        currentAccommodation = buildAccommodationSummaryDto(endDate = today.plusYears(2)),
      )
      val context = EvaluationContext(
        data = data,
        currentResult = buildServiceResult(),
      )

      val expectedContext = EvaluationContext(
        data = data,
        currentResult = buildServiceResult(
          serviceStatus = ServiceStatus.UPCOMING,
          action = "Submit a CRS accommodation referral in 646 days",
        ),
      )

      val result = updater.update(context)

      assertThat(result).isEqualTo(expectedContext)
    }

    @Test
    fun `update returns UPCOMING with generic referral action for non male`() {
      val today = LocalDate.of(2025, 1, 1)
      clock.setNow(today)
      val data = buildDomainData(
        sex = null,
        currentAccommodation = buildAccommodationSummaryDto(endDate = today.plusYears(2)),
      )
      val context = EvaluationContext(
        data = data,
        currentResult = buildServiceResult(),
      )

      val expectedContext = EvaluationContext(
        data = data,
        currentResult = buildServiceResult(
          serviceStatus = ServiceStatus.UPCOMING,
          action = "Submit a CRS referral in 646 days",
        ),
      )

      val result = updater.update(context)

      assertThat(result).isEqualTo(expectedContext)
    }
  }
}
