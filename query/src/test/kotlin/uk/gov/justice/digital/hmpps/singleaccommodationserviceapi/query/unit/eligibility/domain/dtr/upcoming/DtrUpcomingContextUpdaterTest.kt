package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.dtr.upcoming

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.upcoming.DtrUpcomingContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildServiceResult

class DtrUpcomingContextUpdaterTest {
  private val updater = DtrUpcomingContextUpdater()

  @Nested
  inner class UpdateTests {
    @Test
    fun `update returns context UPCOMING`() {
      val data = buildDomainData()
      val context = EvaluationContext(
        data = data,
        currentResult = buildServiceResult(ServiceStatus.NOT_ELIGIBLE),
      )

      val expectedContext = EvaluationContext(
        data = data,
        currentResult = buildServiceResult(ServiceStatus.UPCOMING),
      )

      val result = updater.update(context)

      assertThat(result).isEqualTo(expectedContext)
    }
  }
}
