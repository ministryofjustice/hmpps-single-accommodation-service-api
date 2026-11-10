package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.contextupdaters

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultSpec
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2AssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.sentry.SentryService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.EvaluationContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.Cas2ApplicationData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.DomainDataField

@Component
class Cas2StartContextUpdater(
  private val sentryService: SentryService,
) : ContextUpdater<Cas2ApplicationData>() {
  override val inputFields = setOf(DomainDataField.CAS2_APPLICATION, DomainDataField.CAS2_SUBMITTED_AT, DomainDataField.CAS2_ASSESSMENT_STATUS)

  val notStarted = "notStarted"
  val notSubmitted = "notSubmitted"
  val offerDeclined = "offerDeclined"
  val cancelled = "cancelled"
  val withdrawn = "withdrawn"
  val unknown = "unknown"

  override val outcomes = mapOf(
    notStarted to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CAS2_NOT_STARTED,
    ),
    notSubmitted to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CAS2_NOT_SUBMITTED,
    ),
    offerDeclined to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CAS2_OFFER_DECLINED_OR_WITHDRAWN,
    ),
    cancelled to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CAS2_CANCELLED,
    ),
    withdrawn to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CAS2_WITHDRAWN,
    ),
    unknown to ServiceResultSpec(
      serviceStatus = ServiceStatusNew.CAS2_UNKNOWN,
    ),
  )

  override fun toServiceResult(context: EvaluationContext<Cas2ApplicationData>): ServiceResultNew {
    val application = context.data.cas2Application
    return when {
      application == null -> outcome(notStarted)
      application.submittedApplication?.submittedAt == null -> outcome(notSubmitted)
      application.submittedApplication?.latestAssessmentStatus == Cas2AssessmentStatus.OFFER_DECLINED -> outcome(offerDeclined)
      application.submittedApplication?.latestAssessmentStatus == Cas2AssessmentStatus.CANCELLED -> outcome(cancelled)
      application.submittedApplication?.latestAssessmentStatus == Cas2AssessmentStatus.WITHDRAWN -> outcome(withdrawn)
      else -> outcome(unknown).also {
        val latestAssessmentStatus = application.submittedApplication?.latestAssessmentStatus
        sentryService.captureErrorMessage(
          "CAS2 Service Status unknown because unexpected latest assessment status for ${context.data.crn}: $latestAssessmentStatus",
        )
      }
    }
  }
}
