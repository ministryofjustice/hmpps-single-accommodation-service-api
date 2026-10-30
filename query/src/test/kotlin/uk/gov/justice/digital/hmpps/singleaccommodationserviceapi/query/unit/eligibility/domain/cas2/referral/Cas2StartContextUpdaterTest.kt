package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas2.referral

import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas2Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.sentry.SentryService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.referral.Cas2StartContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildServiceResultNew
import java.util.UUID

class Cas2StartContextUpdaterTest {
  private val sentryService = mockk<SentryService>()

  private val updater = Cas2StartContextUpdater(sentryService)

  @Nested
  inner class UpdateTests {
    @Test
    fun `update builds service result using toServiceResult`() {
      val applicationId = UUID.randomUUID()
      val data = buildDomainData(
        cas2Application = buildCas2Application(
          id = applicationId,
        ),
      )
      val context = EvaluationContext(
        data = data,
        currentResult = buildServiceResultNew(),
      )

      val result = updater.update(context)

      assertThat(result.currentResult!!.serviceStatus).isEqualTo(ServiceStatusNew.CAS2_NOT_SUBMITTED)
      assertThat(result.currentResult.url).isNull()
    }
  }
}
