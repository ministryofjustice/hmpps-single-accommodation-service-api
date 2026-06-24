package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas3.upcoming

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseAction
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseActionType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.upcoming.Cas3UpcomingContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildServiceResult
import java.time.LocalDate

class Cas3UpcomingContextUpdaterTest {
  private val updater = Cas3UpcomingContextUpdater()

  @Nested
  inner class UpdateTests {
    @Test
    fun `update returns context UPCOMING with due date four weeks before end date`() {
      val endDate = LocalDate.of(2025, 1, 1).plusYears(2)
      val data = buildDomainData(
        currentAccommodation = buildAccommodationSummaryDto(endDate = endDate),
      )
      val context = EvaluationContext(
        data = data,
        currentResult = buildServiceResult(),
      )

      val expectedContext = EvaluationContext(
        data = data,
        currentResult = buildServiceResult(
          serviceStatus = ServiceStatus.UPCOMING,
          action = CaseAction(
            type = CaseActionType.START_CAS3_REFERRAL,
            startDate = endDate.minusWeeks(4),
          ),
        ),
      )

      val result = updater.update(context)

      assertThat(result).isEqualTo(expectedContext)
    }
  }
}
