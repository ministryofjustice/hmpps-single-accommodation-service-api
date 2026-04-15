package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas3

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas3Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityKeys
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.Cas3ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.utils.MutableClock
import java.time.LocalDate
import java.util.UUID

class Cas3ContextUpdaterTest {
  private val clock = MutableClock()
  private val updater = Cas3ContextUpdater(clock)

  @Nested
  inner class UpdateTests {
    @Test
    fun `update builds service result using toCas3ServiceResult`() {
      val releaseDate = LocalDate.parse("2026-12-31")
      clock.setNow(releaseDate.minusDays(3))
      val applicationId = UUID.randomUUID()
      val data = buildDomainData(
        releaseDate = releaseDate,
        cas3Application = buildCas3Application(
          id = applicationId,
          applicationStatus = Cas3ApplicationStatus.IN_PROGRESS,
        ),
      )
      val context = EvaluationContext(
        data = data,
        currentResult = ServiceResult(ServiceStatus.NOT_ELIGIBLE),
      )

      val result = updater.update(context)

      assertThat(result.currentResult.action).isNull()
      assertThat(result.currentResult.serviceStatus).isEqualTo(ServiceStatus.NOT_SUBMITTED)
      assertThat(result.currentResult.link).isNotNull()
      assertThat(result.currentResult.link).isEqualTo(EligibilityKeys.VIEW_REFERRAL)
      assertThat(result.currentResult.suitableApplicationId).isEqualTo(applicationId)
    }
  }
}
