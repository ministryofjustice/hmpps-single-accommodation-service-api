package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.dtr.upcoming

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.upcoming.DtrUpcomingContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.utils.MutableClock
import java.time.LocalDate

class DtrUpcomingContextUpdaterTest {
  private val clock = MutableClock()
  private val updater = DtrUpcomingContextUpdater(clock)

  @Nested
  inner class UpdateTests {
    @Test
    fun `update returns context UPCOMING`() {
      val today = LocalDate.of(2025, 1, 1)
      clock.setNow(today)
      val data = buildDomainData(
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
          action = "Submit a DTR referral in 674 days",
        ),
      )

      val result = updater.update(context)

      assertThat(result).isEqualTo(expectedContext)
    }
  }
}
