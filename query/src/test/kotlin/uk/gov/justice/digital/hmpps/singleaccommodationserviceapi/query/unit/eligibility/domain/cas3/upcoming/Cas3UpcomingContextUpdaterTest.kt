package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas3.upcoming

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.upcoming.Cas3UpcomingContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildServiceResultNew
import java.time.LocalDate

class Cas3UpcomingContextUpdaterTest {
  private val updater = Cas3UpcomingContextUpdater()

  @Test
  fun `update returns CAS3 upcoming and action date 15 days before accommodation end date`() {
    val endDate = LocalDate.parse("2025-01-30")
    val data = buildDomainData(
      currentAccommodation = buildAccommodationSummaryDto(endDate = endDate),
    )
    val context = EvaluationContext(
      data = data,
      currentResult = buildServiceResultNew(),
    )

    val result = updater.update(context)

    assertThat(result.currentResult!!.serviceStatus).isEqualTo(ServiceStatusNew.CAS3_UPCOMING)
    assertThat(result.currentResult.actionStartDate).isEqualTo(LocalDate.parse("2025-01-15"))
  }
}
