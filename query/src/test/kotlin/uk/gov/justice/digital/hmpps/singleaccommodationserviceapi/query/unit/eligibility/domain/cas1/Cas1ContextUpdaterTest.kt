package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas1

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityKeys
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.Cas1ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.utils.MutableClock
import java.time.LocalDate
import java.util.UUID

class Cas1ContextUpdaterTest {
  private val clock = MutableClock()
  private val updater = Cas1ContextUpdater(clock)

  @Nested
  inner class UpdateTests {
    @Test
    fun `update builds service result using toCas1ServiceResult`() {
      val releaseDate = LocalDate.parse("2026-12-31")
      clock.setNow(releaseDate.minusDays(3))
      val applicationId = UUID.randomUUID()
      val data = buildDomainData(
        releaseDate = releaseDate,
        cas1Application = buildCas1Application(
          id = applicationId,
          applicationStatus = Cas1ApplicationStatus.STARTED,
        ),
      )
      val context = EvaluationContext(
        data = data,
        currentResult = ServiceResult(ServiceStatus.NOT_ELIGIBLE),
      )

      val result = updater.update(context)

      assertThat(result.currentResult.action).isNotNull()
      assertThat(result.currentResult.action).isEqualTo(EligibilityKeys.CONTINUE_APPROVED_PREMISE_APPLICATION)
      assertThat(result.currentResult.serviceStatus).isEqualTo(ServiceStatus.NOT_SUBMITTED)
      assertThat(result.currentResult.link).isNotNull()
      assertThat(result.currentResult.link).isEqualTo(EligibilityKeys.CONTINUE_APPLICATION)
      assertThat(result.currentResult.suitableApplicationId).isEqualTo(applicationId)
    }
  }
}
