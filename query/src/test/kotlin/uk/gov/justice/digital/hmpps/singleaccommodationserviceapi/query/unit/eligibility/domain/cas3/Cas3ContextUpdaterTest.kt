package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas3

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas3Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.ActionKeys.CREATE_PLACEMENT
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.ActionKeys.PROVIDE_INFORMATION
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.ActionKeys.START_CAS3_REFERRAL
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.Cas3ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factory.buildDomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.utils.MutableClock
import java.time.LocalDate
import java.util.UUID

class Cas3ContextUpdaterTest {
  private val clock = MutableClock()
  private val updater = Cas3ContextUpdater(clock)

  @Nested
  inner class UpdateTests {
    @Test
    fun `update builds action using buildActionForCas3`() {
      val releaseDate = LocalDate.now().plusDays(20)
      clock.setNow(releaseDate.minusDays(10))
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
        currentResult = ServiceResult(ServiceStatus.CONFIRMED),
      )

      val result = updater.update(context)

      assertThat(result.currentResult.action).isNotNull()
      assertThat(result.currentResult.action?.text).isEqualTo(START_CAS3_REFERRAL)
    }

    @Test
    fun `update calculates service status using toServiceStatus with application status`() {
      val releaseDate = LocalDate.now().plusDays(20)
      clock.setNow(releaseDate.minusDays(10))
      val data = buildDomainData(
        releaseDate = releaseDate,
        cas3Application = buildCas3Application(
          applicationStatus = Cas3ApplicationStatus.AWAITING_PLACEMENT,
        ),
      )
      val context = EvaluationContext(
        data = data,
        currentResult = ServiceResult(ServiceStatus.NOT_ELIGIBLE),
      )

      val result = updater.update(context)

      assertThat(result.currentResult.serviceStatus).isEqualTo(ServiceStatus.CONFIRMED)
    }

    @Test
    fun `update sets suitableApplicationId from cas3Application id`() {
      val releaseDate = LocalDate.now().plusDays(20)
      clock.setNow(releaseDate.minusDays(10))
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

      assertThat(result.currentResult.suitableApplicationId).isEqualTo(applicationId)
    }

    @Test
    fun `update sets suitableApplicationId to null when cas3Application is null`() {
      val releaseDate = LocalDate.now().plusDays(20)
      clock.setNow(releaseDate.minusDays(10))
      val data = buildDomainData(
        releaseDate = releaseDate,
        cas3Application = null,
      )
      val context = EvaluationContext(
        data = data,
        currentResult = ServiceResult(ServiceStatus.NOT_ELIGIBLE),
      )

      val result = updater.update(context)

      assertThat(result.currentResult.suitableApplicationId).isNull()
    }

    @Test
    fun `update returns updated context with new ServiceResult`() {
      val releaseDate = LocalDate.now().plusDays(20)
      clock.setNow(releaseDate.minusDays(10))
      val data = buildDomainData(
        releaseDate = releaseDate,
        cas3Application = buildCas3Application(
          applicationStatus = Cas3ApplicationStatus.AWAITING_PLACEMENT,
        ),
      )
      val originalContext = EvaluationContext(
        data = data,
        currentResult = ServiceResult(ServiceStatus.NOT_ELIGIBLE),
      )

      val result = updater.update(originalContext)

      assertThat(result.data).isEqualTo(originalContext.data)
      assertThat(result.currentResult).isNotEqualTo(originalContext.currentResult)
      assertThat(result.currentResult.serviceStatus).isEqualTo(ServiceStatus.CONFIRMED)
    }

    @Test
    fun `update handles PLACED status with completed placement correctly`() {
      val releaseDate = LocalDate.now().plusDays(20)
      clock.setNow(releaseDate.minusDays(10))
      val data = buildDomainData(
        releaseDate = releaseDate,
        cas3Application = buildCas3Application(
          applicationStatus = Cas3ApplicationStatus.PLACED,
          placementStatus = Cas3PlacementStatus.DEPARTED,
        ),
      )
      val context = EvaluationContext(
        data = data,
        currentResult = ServiceResult(ServiceStatus.NOT_ELIGIBLE),
      )

      val result = updater.update(context)

      assertThat(result.currentResult.serviceStatus).isEqualTo(ServiceStatus.CONFIRMED)
      assertThat(result.currentResult.action?.text).isEqualTo(CREATE_PLACEMENT)
    }

    @Test
    fun `update handles REJECTED status correctly`() {
      val releaseDate = LocalDate.now().plusDays(20)
      clock.setNow(releaseDate.minusDays(10))
      val data = buildDomainData(
        releaseDate = releaseDate,
        cas3Application = buildCas3Application(
          applicationStatus = Cas3ApplicationStatus.REJECTED,
        ),
      )
      val context = EvaluationContext(
        data = data,
        currentResult = ServiceResult(ServiceStatus.NOT_ELIGIBLE),
      )

      val result = updater.update(context)

      assertThat(result.currentResult.serviceStatus).isEqualTo(ServiceStatus.REJECTED)
    }

    @Test
    fun `update handles WITHDRAWN status correctly`() {
      val releaseDate = LocalDate.now().plusDays(20)
      clock.setNow(releaseDate.minusDays(10))
      val data = buildDomainData(
        releaseDate = releaseDate,
        cas3Application = buildCas3Application(
          applicationStatus = Cas3ApplicationStatus.WITHDRAWN,
        ),
      )
      val context = EvaluationContext(
        data = data,
        currentResult = ServiceResult(ServiceStatus.NOT_ELIGIBLE),
      )

      val result = updater.update(context)

      assertThat(result.currentResult.serviceStatus).isEqualTo(ServiceStatus.WITHDRAWN)
    }

    @Test
    fun `update handles PENDING status correctly`() {
      val releaseDate = LocalDate.now().plusDays(20)
      clock.setNow(releaseDate.minusDays(10))
      val data = buildDomainData(
        releaseDate = releaseDate,
        cas3Application = buildCas3Application(
          applicationStatus = Cas3ApplicationStatus.PENDING,
        ),
      )
      val context = EvaluationContext(
        data = data,
        currentResult = ServiceResult(ServiceStatus.NOT_ELIGIBLE),
      )

      val result = updater.update(context)

      assertThat(result.currentResult.serviceStatus).isEqualTo(ServiceStatus.SUBMITTED)
      assertThat(result.currentResult.action?.text).isEqualTo(CREATE_PLACEMENT)
    }

    @Test
    fun `update handles REQUESTED_FURTHER_INFORMATION status correctly`() {
      val releaseDate = LocalDate.now().plusDays(20)
      clock.setNow(releaseDate.minusDays(10))
      val data = buildDomainData(
        releaseDate = releaseDate,
        cas3Application = buildCas3Application(
          applicationStatus = Cas3ApplicationStatus.REQUESTED_FURTHER_INFORMATION,
        ),
      )
      val context = EvaluationContext(
        data = data,
        currentResult = ServiceResult(ServiceStatus.NOT_ELIGIBLE),
      )

      val result = updater.update(context)

      assertThat(result.currentResult.serviceStatus).isEqualTo(ServiceStatus.SUBMITTED)
      assertThat(result.currentResult.action?.text).isEqualTo(PROVIDE_INFORMATION)
    }
  }
}
