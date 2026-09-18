package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas2.suitability

import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseAction
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseActionType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.LinkType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas2Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.sentry.SentryService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityKeys
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.suitability.Cas2SuitabilityContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildServiceResultNew
import java.util.UUID

class Cas2SuitabilityContextUpdaterTest {
  private val sentryService = mockk<SentryService>()

  private val updater = Cas2SuitabilityContextUpdater(sentryService)

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

      assertThat(result.currentResult.action).isEqualTo(CaseAction(type = CaseActionType.CONTINUE_A_CAS2_REFERRAL, service = AccommodationService.CAS2))
      assertThat(result.currentResult.serviceStatus).isEqualTo(ServiceStatusNew.CAS2_NOT_SUBMITTED)
      assertThat(result.currentResult.link).isEqualTo(EligibilityKeys.CONTINUE_APPLICATION)
      assertThat(result.currentResult.linkType).isEqualTo(LinkType.CAS2_VIEW_APPLICATION)
      assertThat(result.currentResult.url).isNull()
    }
  }
}
