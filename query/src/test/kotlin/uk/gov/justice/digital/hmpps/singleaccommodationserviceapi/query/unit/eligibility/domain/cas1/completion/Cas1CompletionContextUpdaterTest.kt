package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas1.completion

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityKeys
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.completion.Cas1CompletionContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildCurrentAccommodation
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.utils.MutableClock
import java.time.LocalDate
import java.util.UUID

class Cas1CompletionContextUpdaterTest {
  private val clock = MutableClock()
  private val updater = Cas1CompletionContextUpdater()

  @Nested
  inner class UpdateTests {
    @Test
    fun `update builds service result using toServiceResult`() {
      val currentAccommodationEndDate = LocalDate.parse("2026-12-31")
      clock.setNow(currentAccommodationEndDate.minusDays(3))
      val applicationId = UUID.randomUUID()
      val data = buildDomainData(
        currentAccommodation = buildCurrentAccommodation(currentAccommodationEndDate, true),
        cas1Application = buildCas1Application(
          id = applicationId,
          applicationStatus = Cas1ApplicationStatus.AWAITING_ASSESSMENT,
        ),
      )
      val context = EvaluationContext(
        data = data,
        currentResult = ServiceResult(ServiceStatus.NOT_ELIGIBLE),
      )

      val result = updater.update(context)

      Assertions.assertThat(result.currentResult.serviceStatus).isEqualTo(ServiceStatus.SUBMITTED)
      Assertions.assertThat(result.currentResult.action).isNotNull()
      Assertions.assertThat(result.currentResult.action).isEqualTo(EligibilityKeys.WAIT_FOR_ASSESSMENT_RESULT)
      Assertions.assertThat(result.currentResult.link).isNotNull()
      Assertions.assertThat(result.currentResult.link).isEqualTo(EligibilityKeys.VIEW_APPLICATION)
      Assertions.assertThat(result.currentResult.suitableApplicationId).isEqualTo(applicationId)
    }
  }
}
