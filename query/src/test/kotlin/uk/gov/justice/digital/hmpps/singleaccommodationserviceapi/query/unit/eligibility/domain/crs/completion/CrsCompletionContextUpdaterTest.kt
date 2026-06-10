package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.crs.completion

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityKeys
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.completion.CrsCompletionContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildServiceResult

class CrsCompletionContextUpdaterTest {
  val crsUiUrl = "CRS_UI_URL"
  private val updater = CrsCompletionContextUpdater(crsUiUrl)

  @Nested
  inner class UpdateTests {
    @Test
    fun `update returns NOT_STARTED with accommodation referral action for male`() {
      val data = buildDomainData(sex = SexCode.M)
      val context = EvaluationContext(
        data = data,
        currentResult = buildServiceResult(),
      )

      val expectedContext = EvaluationContext(
        data = data,
        currentResult = buildServiceResult(
          serviceStatus = ServiceStatus.NOT_STARTED,
          action = EligibilityKeys.SUBMIT_CRS_ACCOMMODATION_REFERRAL,
          link = EligibilityKeys.VIEW_REFER_AND_MONITOR,
          url = crsUiUrl,
        ),
      )

      val result = updater.update(context)

      assertThat(result).isEqualTo(expectedContext)
    }

    @Test
    fun `update returns NOT_STARTED with generic referral action for non male`() {
      val data = buildDomainData(sex = null)
      val context = EvaluationContext(
        data = data,
        currentResult = buildServiceResult(),
      )

      val expectedContext = EvaluationContext(
        data = data,
        currentResult = buildServiceResult(
          serviceStatus = ServiceStatus.NOT_STARTED,
          action = EligibilityKeys.SUBMIT_CRS_REFERRAL,
          link = EligibilityKeys.VIEW_REFER_AND_MONITOR,
          url = crsUiUrl,
        ),
      )

      val result = updater.update(context)

      assertThat(result).isEqualTo(expectedContext)
    }
  }
}
