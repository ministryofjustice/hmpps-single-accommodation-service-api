package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas1.upcoming

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.upcoming.Cas1UpcomingContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.utils.MutableClock
import java.time.LocalDate

class Cas1UpcomingContextUpdaterTest {
  private val clock = MutableClock()
  private val updater = Cas1UpcomingContextUpdater(clock)

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
          action = "Start an approved premises (CAS1) application in 365 days",
        ),
      )

      val result = updater.update(context)

      assertThat(result).isEqualTo(expectedContext)
    }
  }
}
