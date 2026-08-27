package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas2.completion

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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas2.completion.Cas2CompletionContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildServiceResult
import java.util.UUID

class Cas2CompletionContextUpdaterTest {
  private val updater = Cas2CompletionContextUpdater()

  @Nested
  inner class UpdateTests {
    @Test
    fun `update builds service result using toServiceResult`() {
      val applicationId = UUID.randomUUID()
      val data = buildDomainData(
        cas2Application = buildCas2Application(
          application = buildCas2ApplicationSummary(status = "COMPLETED", id = applicationId),
        ),
      )
      val context = EvaluationContext(
        data = data,
        currentResult = buildServiceResult(),
      )

      val result = updater.update(context)

      assertThat(result.currentResult.serviceStatus).isEqualTo(ServiceStatus.STARTED)
      assertThat(result.currentResult.action).isEqualTo(CaseAction(type = CaseActionType.CONTINUE_CAS2_APPLICATION))
      assertThat(result.currentResult.link).isEqualTo(EligibilityKeys.CONTINUE_APPLICATION)
      assertThat(result.currentResult.linkType).isEqualTo(LinkType.CAS2_VIEW_APPLICATION)
      assertThat(result.currentResult.url).isNull()
    }
  }
}
