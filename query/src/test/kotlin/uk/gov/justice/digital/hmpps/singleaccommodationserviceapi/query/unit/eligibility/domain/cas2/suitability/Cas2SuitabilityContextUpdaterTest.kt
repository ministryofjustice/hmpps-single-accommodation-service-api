package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas2.suitability

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseAction
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseActionType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.LinkType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas2Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas2ApplicationSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityKeys
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.suitability.Cas2SuitabilityContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildServiceResult
import java.util.UUID

class Cas2SuitabilityContextUpdaterTest {
  private val updater = Cas2SuitabilityContextUpdater()

  @Nested
  inner class UpdateTests {
    @Test
    fun `update builds service result using toServiceResult`() {
      val applicationId = UUID.randomUUID()
      val data = buildDomainData(
        cas2Application = buildCas2Application(
          application = buildCas2ApplicationSummary(status = "STARTED", id = applicationId),
        ),
      )
      val context = EvaluationContext(
        data = data,
        currentResult = buildServiceResult(),
      )

      val result = updater.update(context)

      assertThat(result.currentResult.action).isEqualTo(CaseAction(type = CaseActionType.START_CAS2_APPLICATION))
      assertThat(result.currentResult.serviceStatus).isEqualTo(ServiceStatus.NOT_STARTED)
      assertThat(result.currentResult.link).isEqualTo(EligibilityKeys.START_APPLICATION)
      assertThat(result.currentResult.linkType).isEqualTo(LinkType.CAS2_START_APPLICATION)
      assertThat(result.currentResult.url).isNull()
    }
  }
}
